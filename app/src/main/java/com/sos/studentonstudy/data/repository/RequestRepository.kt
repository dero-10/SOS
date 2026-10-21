package com.sos.studentonstudy.data.repository

import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.RequestDao
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.RequestWithMeta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Help requests: the logged-in user's own (CRUD) and the public feed from everyone. */
@OptIn(ExperimentalCoroutinesApi::class)
class RequestRepository(private val requestDao: RequestDao, private val session: SessionManager) {
    /** Every public request, newest first; what Explore → Request shows. */
    val feed: Flow<List<RequestWithMeta>> = requestDao.observeFeed()

    /** Requests the logged-in user created, including ones sent privately to a helper. */
    val myRequests: Flow<List<RequestWithMeta>> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else requestDao.observeOwnWithMeta(id)
    }

    val currentUserId: Long? get() = session.userId.value

    fun observe(id: Long): Flow<RequestWithMeta?> = requestDao.observeWithMeta(id)

    suspend fun get(id: Long): RequestEntity? = requestDao.getById(id)

    suspend fun save(request: RequestEntity) {
        if (request.id == 0L) {
            val userId = session.userId.value ?: return
            requestDao.insert(request.copy(userId = userId))
        } else {
            requestDao.update(request)
        }
    }

    suspend fun delete(request: RequestEntity) = requestDao.delete(request)
}
