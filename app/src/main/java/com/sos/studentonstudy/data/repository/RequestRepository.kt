package com.sos.studentonstudy.data.repository

import android.util.Log
import com.sos.studentonstudy.data.RequestImageFiles
import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.RequestDao
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.RequestWithMeta
import com.sos.studentonstudy.data.local.UserDao
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.data.local.tagList
import com.sos.studentonstudy.data.remote.ProfileDto
import com.sos.studentonstudy.data.remote.REQUEST_COLUMNS
import com.sos.studentonstudy.data.remote.REQUEST_IMAGES_BUCKET
import com.sos.studentonstudy.data.remote.RequestDto
import com.sos.studentonstudy.data.remote.RequestWriteDto
import com.sos.studentonstudy.data.remote.deadlineFromIso
import com.sos.studentonstudy.data.remote.deadlineToIso
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import io.ktor.http.defaultForFileExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Contract used by request screens and ViewModels; tests can supply in-memory requests. */
interface RequestRepository {
    val feed: Flow<List<RequestWithMeta>>
    val myRequests: Flow<List<RequestWithMeta>>
    val currentUserId: Long?
    fun observe(id: Long): Flow<RequestWithMeta?>
    suspend fun get(id: Long): RequestEntity?
    suspend fun refresh(): Boolean
    suspend fun save(request: RequestEntity): Boolean
    suspend fun delete(request: RequestEntity): Boolean
    suspend fun sweepImages()
}

/**
 * Help requests: the logged-in user's own (CRUD) and the public feed from everyone.
 *
 * With a [remote] Supabase client the server is the source of truth: writes go to Supabase first,
 * and [refresh] mirrors the feed and the user's own requests into Room (`requests.remoteId`),
 * which is what the screens observe. Without one, everything stays in Room as before.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RequestRepositoryImpl(
    private val requestDao: RequestDao,
    private val userDao: UserDao,
    private val session: SessionManager,
    private val imageFiles: RequestImageFiles,
    private val remote: SupabaseClient?
) : RequestRepository {
    private val online = remote != null
    private val refreshLock = Mutex()

    /** Every public request, newest first; what Explore → Request shows. */
    override val feed: Flow<List<RequestWithMeta>> = requestDao.observeFeed(online)

    /** Requests the logged-in user created, including ones sent privately to a helper. */
    override val myRequests: Flow<List<RequestWithMeta>> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else requestDao.observeOwnWithMeta(id, online)
    }

    override val currentUserId: Long? get() = session.userId.value

    override fun observe(id: Long): Flow<RequestWithMeta?> = requestDao.observeWithMeta(id)

    override suspend fun get(id: Long): RequestEntity? = requestDao.getById(id)

    /**
     * Pulls the newest public requests and all of the user's own requests from Supabase into Room,
     * and drops cached rows that were deleted on the server. Returns false when offline or on error.
     */
    override suspend fun refresh(): Boolean {
        val client = remote ?: return true
        client.auth.awaitInitialization()
        val uid = client.auth.currentUserOrNull()?.id ?: return false
        return refreshLock.withLock {
            runCatching {
                val feed = client.from(REQUESTS).select(Columns.raw(REQUEST_COLUMNS)) {
                    filter { exact("helper_id", null) }
                    order("created_at", Order.DESCENDING)
                    limit(FEED_LIMIT)
                }.decodeList<RequestDto>()
                val own = client.from(REQUESTS).select(Columns.raw(REQUEST_COLUMNS)) {
                    filter {
                        eq("user_id", uid)
                        exact("helper_id", null)
                    }
                }.decodeList<RequestDto>()
                val rows = (feed + own).distinctBy { it.id }
                rows.forEach { cache(client, it) }
                val keep = rows.map { it.id }.toSet()
                requestDao.allRemote().filter { it.remoteId !in keep }.forEach { stale ->
                    requestDao.delete(stale)
                    imageFiles.delete(stale.imagePath)
                }
            }.onFailure { Log.w(TAG, "Request refresh failed", it) }.isSuccess
        }
    }

    /** Creates or updates [request]. Returns false when it could not be saved (no connection, server error). */
    override suspend fun save(request: RequestEntity): Boolean {
        val userId = session.userId.value ?: return false
        val client = remote
        val original = if (request.id == 0L) null else requestDao.getById(request.id)
        if (client == null || (original != null && original.remoteId == null)) {
            saveLocal(request, userId, original)
            return true
        }
        val uid = client.auth.currentUserOrNull()?.id ?: return false
        return runCatching {
            // A newly picked picture is a local file: upload it and point the request at the stored object.
            val imageObject = when {
                request.imagePath == null -> null
                request.imagePath == original?.imagePath -> objectPath(request.imagePath)
                else -> uploadImage(client, uid, request.imagePath)
            }
            val body = RequestWriteDto(
                userId = uid,
                title = request.title,
                details = request.details,
                tags = request.tagList(),
                estimatedTime = request.estimatedTime,
                deadline = deadlineToIso(request.deadline),
                budget = request.budget,
                attachmentName = request.attachmentName,
                imagePath = imageObject
            )
            val saved = if (original == null) {
                client.from(REQUESTS).insert(body) { select(Columns.raw(REQUEST_COLUMNS)) }.decodeSingle<RequestDto>()
            } else {
                client.from(REQUESTS).update(body) {
                    select(Columns.raw(REQUEST_COLUMNS))
                    filter { eq("id", original.remoteId!!) }
                }.decodeSingle<RequestDto>()
            }
            // The old picture is no longer referenced on the server.
            val oldObject = original?.imagePath?.let(::objectPath)
            if (oldObject != null && oldObject != imageObject) deleteObject(client, oldObject)
            cache(client, saved)
            // The uploaded copy now lives on the server; the local file is no longer needed.
            if (request.imagePath != null && request.imagePath != original?.imagePath) imageFiles.delete(request.imagePath)
        }.onFailure { Log.w(TAG, "Saving request failed", it) }.isSuccess
    }

    /** Deletes [request]. Returns false when the server copy could not be deleted. */
    override suspend fun delete(request: RequestEntity): Boolean {
        val client = remote
        val remoteId = request.remoteId
        if (client != null && remoteId != null) {
            val deleted = runCatching {
                client.from(REQUESTS).delete { filter { eq("id", remoteId) } }
                request.imagePath?.let(::objectPath)?.let { deleteObject(client, it) }
            }.onFailure { Log.w(TAG, "Deleting request failed", it) }.isSuccess
            if (!deleted) return false
        }
        requestDao.delete(request)
        imageFiles.delete(request.imagePath)
        return true
    }

    /** Removes picture files that no request uses any more. */
    override suspend fun sweepImages() {
        imageFiles.sweepOrphans()
    }

    // --- Offline ----------------------------------------------------------------------------

    private suspend fun saveLocal(request: RequestEntity, userId: Long, original: RequestEntity?) {
        if (original == null) {
            requestDao.insert(request.copy(userId = userId))
        } else {
            requestDao.update(request)
            if (original.imagePath != request.imagePath) imageFiles.delete(original.imagePath)
        }
    }

    // --- Online helpers ---------------------------------------------------------------------

    /** Writes one server row into Room, caching its author first so the `users` foreign key holds. */
    private suspend fun cache(client: SupabaseClient, dto: RequestDto) {
        val authorId = dto.author?.let { cacheAuthor(it) } ?: return
        val existing = requestDao.findByRemoteId(dto.id)
        val entity = RequestEntity(
            id = existing?.id ?: 0,
            userId = authorId,
            title = dto.title,
            details = dto.details,
            tags = dto.tags.joinToString(","),
            estimatedTime = dto.estimatedTime,
            deadline = deadlineFromIso(dto.deadline),
            budget = dto.budget,
            attachmentName = dto.attachmentName,
            imagePath = dto.imagePath?.let { client.storage.from(REQUEST_IMAGES_BUCKET).publicUrl(it) },
            createdAt = dto.createdAt.toEpochMilliseconds(),
            remoteId = dto.id
        )
        if (existing == null) requestDao.insert(entity) else requestDao.update(entity)
    }

    /** Inserts or updates the Room copy of another user's public profile and returns its local id. */
    private suspend fun cacheAuthor(profile: ProfileDto): Long {
        // Room needs a unique email; fall back to a placeholder when the profile has none.
        val email = profile.email.trim().lowercase().ifEmpty { "${profile.id}@users.sos.invalid" }
        val existing = userDao.findByRemoteId(profile.id) ?: userDao.findByEmail(email)
        if (existing == null) {
            return userDao.insert(
                UserEntity(
                    name = profile.name, email = email, passwordHash = "", major = profile.major,
                    university = profile.university, linkedinUrl = profile.linkedinUrl, balance = 0, remoteId = profile.id
                )
            )
        }
        userDao.updatePublicProfile(
            existing.id, profile.id, profile.name, email, profile.major, profile.university, profile.linkedinUrl
        )
        return existing.id
    }

    private suspend fun uploadImage(client: SupabaseClient, uid: String, localPath: String): String {
        val file = File(localPath)
        val extension = file.extension.ifEmpty { "jpg" }
        val objectPath = "$uid/${UUID.randomUUID()}.$extension"
        val bytes = withContext(Dispatchers.IO) { file.readBytes() }
        client.storage.from(REQUEST_IMAGES_BUCKET).upload(objectPath, bytes) {
            contentType = ContentType.defaultForFileExtension(extension)
        }
        return objectPath
    }

    private suspend fun deleteObject(client: SupabaseClient, objectPath: String) {
        runCatching { client.storage.from(REQUEST_IMAGES_BUCKET).delete(objectPath) }
            .onFailure { Log.w(TAG, "Could not delete $objectPath from storage", it) }
    }

    /** The storage object path inside a public URL, or null for a local file path. */
    private fun objectPath(imagePath: String): String? =
        imagePath.substringAfter("/object/public/$REQUEST_IMAGES_BUCKET/", missingDelimiterValue = "").ifEmpty { null }

    private companion object {
        const val TAG = "RequestRepository"
        const val REQUESTS = "requests"
        const val FEED_LIMIT = 100L
    }
}
