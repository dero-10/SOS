package com.sos.studentonstudy.data.repository

import android.util.Log
import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.UserDao
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.data.remote.ProfileDto
import com.sos.studentonstudy.data.remote.ProfileUpdateDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import java.security.MessageDigest

enum class AuthResult {
    Success,
    /** Offline mode only: no local account with this email. */
    UnknownEmail,
    /** Offline mode only: the password does not match. */
    WrongPassword,
    /** Online mode: Supabase does not say which of the two is wrong. */
    InvalidCredentials,
    EmailTaken,
    /** Online mode with "Confirm email" switched on: the user must click the link first. */
    ConfirmEmail,
    NetworkError,
    ServerError
}

/** What the screens and ViewModels need from the account layer; lets tests swap in a fake. */
interface AuthRepository {
    val isOnline: Boolean
    val isLoggedIn: Boolean
    val currentUser: Flow<UserEntity?>
    suspend fun login(email: String, password: String): AuthResult
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun updateProfile(name: String, major: String, university: String, linkedinUrl: String): Boolean
    suspend fun topUp(amount: Long): Boolean
    suspend fun withdraw(amount: Long): Boolean
    suspend fun syncSession()
    fun logout()
}

/**
 * Accounts and the logged-in profile.
 *
 * With a [remote] Supabase client, sign-in/sign-up/profile/balance go to the server and the
 * result is cached in Room (`users.remoteId`), so the rest of the app keeps reading Room Flows.
 * Without one, everything stays local as before (SHA-256 hash in Room).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val session: SessionManager,
    private val remote: SupabaseClient?,
    private val appScope: CoroutineScope
) : AuthRepository {
    override val isOnline: Boolean get() = remote != null

    override val isLoggedIn: Boolean get() = session.userId.value != null

    override val currentUser: Flow<UserEntity?> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.observe(id)
    }

    override suspend fun login(email: String, password: String): AuthResult {
        val client = remote ?: return loginLocal(email, password)
        return remoteCall {
            client.auth.signInWith(Email) {
                this.email = normalize(email)
                this.password = password
            }
            cacheProfileAndStartSession(client)
        }
    }

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        val client = remote ?: return registerLocal(name, email, password)
        return remoteCall {
            client.auth.signUpWith(Email) {
                this.email = normalize(email)
                this.password = password
                data = buildJsonObject { put("name", name.trim()) }
            }
            // No session means the project requires email confirmation before the first login.
            if (client.auth.currentSessionOrNull() == null) AuthResult.ConfirmEmail
            else cacheProfileAndStartSession(client)
        }
    }

    /** Returns false when the change could not be saved (offline or server error). */
    override suspend fun updateProfile(name: String, major: String, university: String, linkedinUrl: String): Boolean {
        val id = session.userId.value ?: return false
        val client = remote
        if (client != null) {
            val uid = client.auth.currentUserOrNull()?.id ?: return false
            val saved = runCatching {
                client.from(PROFILES).update(ProfileUpdateDto(name.trim(), major.trim(), university.trim(), linkedinUrl.trim())) {
                    filter { eq("id", uid) }
                }
            }.onFailure { Log.w(TAG, "Profile update failed", it) }.isSuccess
            if (!saved) return false
        }
        userDao.updateProfile(id, name.trim(), major.trim(), university.trim(), linkedinUrl.trim())
        return true
    }

    /** Adds credits to the balance (simulated purchase, there is no payment gateway yet). */
    override suspend fun topUp(amount: Long): Boolean = adjustBalance(amount)

    /** Removes [amount] from the balance; false when the balance is too low. */
    override suspend fun withdraw(amount: Long): Boolean = adjustBalance(-amount)

    private suspend fun adjustBalance(delta: Long): Boolean {
        val id = session.userId.value ?: return false
        val client = remote ?: return userDao.adjustBalance(id, delta) > 0
        // The server owns the balance: `adjust_balance` returns the new value, or null when it would go negative.
        val newBalance = runCatching {
            // The body is the bare number, or `null` when the balance would go below zero.
            client.postgrest.rpc("adjust_balance", buildJsonObject { put("delta", delta) }).data.trim().toLongOrNull()
        }.onFailure { Log.w(TAG, "adjust_balance failed", it) }.getOrNull() ?: return false
        userDao.setBalance(id, newBalance)
        return true
    }

    /**
     * Called once at app start. Drops the local session when the Supabase session is gone
     * (expired refresh token, signed out elsewhere) and refreshes the cached profile otherwise.
     */
    override suspend fun syncSession() {
        val client = remote ?: return
        val id = session.userId.value ?: return
        client.auth.awaitInitialization()
        if (client.auth.currentUserOrNull() == null) {
            session.clear()
            return
        }
        runCatching { cacheProfileAndStartSession(client) }
            .onFailure { Log.w(TAG, "Profile refresh failed for user $id, keeping the cached copy", it) }
    }

    override fun logout() {
        session.clear()
        val client = remote ?: return
        appScope.launch {
            runCatching { client.auth.signOut() }.onFailure { Log.w(TAG, "Remote sign-out failed", it) }
        }
    }

    // --- Online -----------------------------------------------------------------------------

    private suspend fun cacheProfileAndStartSession(client: SupabaseClient): AuthResult {
        val uid = client.auth.currentUserOrNull()?.id ?: return AuthResult.ServerError
        val profile = client.from(PROFILES).select { filter { eq("id", uid) } }.decodeSingleOrNull<ProfileDto>()
        if (profile == null) {
            // The `on_auth_user_created` trigger did not run: supabase/schema.sql has not been applied.
            Log.e(TAG, "No profiles row for $uid — run supabase/schema.sql in the Supabase SQL editor")
            return AuthResult.ServerError
        }
        session.start(cacheProfile(profile))
        return AuthResult.Success
    }

    /** Inserts or updates the Room copy of [profile] and returns its local id. */
    private suspend fun cacheProfile(profile: ProfileDto): Long {
        val email = normalize(profile.email)
        // An offline account with the same email is adopted rather than duplicated (email is unique in Room).
        val existing = userDao.findByRemoteId(profile.id) ?: userDao.findByEmail(email)
        if (existing == null) {
            return userDao.insert(
                UserEntity(
                    name = profile.name, email = email, passwordHash = "",
                    major = profile.major, university = profile.university,
                    linkedinUrl = profile.linkedinUrl, balance = profile.balance, remoteId = profile.id
                )
            )
        }
        userDao.updateFromRemote(
            existing.id, profile.id, profile.name, email, profile.major,
            profile.university, profile.linkedinUrl, profile.balance
        )
        return existing.id
    }

    private inline fun remoteCall(block: () -> AuthResult): AuthResult = try {
        block()
    } catch (e: AuthRestException) {
        Log.w(TAG, "Auth error ${e.errorCode}", e)
        when (e.errorCode) {
            AuthErrorCode.InvalidCredentials -> AuthResult.InvalidCredentials
            AuthErrorCode.EmailNotConfirmed -> AuthResult.ConfirmEmail
            AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> AuthResult.EmailTaken
            else -> AuthResult.ServerError
        }
    } catch (e: HttpRequestException) {
        Log.w(TAG, "Network error", e)
        AuthResult.NetworkError
    } catch (e: HttpRequestTimeoutException) {
        Log.w(TAG, "Timeout", e)
        AuthResult.NetworkError
    } catch (e: IOException) {
        Log.w(TAG, "Network error", e)
        AuthResult.NetworkError
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected auth failure", e)
        AuthResult.ServerError
    }

    // --- Offline ----------------------------------------------------------------------------

    private suspend fun loginLocal(email: String, password: String): AuthResult {
        val user = userDao.findByEmail(normalize(email)) ?: return AuthResult.UnknownEmail
        if (user.passwordHash != hashPassword(password)) return AuthResult.WrongPassword
        session.start(user.id)
        return AuthResult.Success
    }

    private suspend fun registerLocal(name: String, email: String, password: String): AuthResult {
        val normalized = normalize(email)
        if (userDao.findByEmail(normalized) != null) return AuthResult.EmailTaken
        val id = userDao.insert(UserEntity(name = name.trim(), email = normalized, passwordHash = hashPassword(password)))
        session.start(id)
        return AuthResult.Success
    }

    private fun normalize(email: String) = email.trim().lowercase()

    private companion object {
        const val TAG = "AuthRepository"
        const val PROFILES = "profiles"
    }
}

internal fun hashPassword(password: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .joinToString("") { "%02x".format(it) }
