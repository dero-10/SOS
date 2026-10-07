package com.sos.studentonstudy.data.repository

import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.UserDao
import com.sos.studentonstudy.data.local.UserEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.security.MessageDigest

enum class AuthResult { Success, UnknownEmail, WrongPassword, EmailTaken }

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepository(private val userDao: UserDao, private val session: SessionManager) {
    val isLoggedIn: Boolean get() = session.userId.value != null

    val currentUser: Flow<UserEntity?> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(null) else userDao.observe(id)
    }

    suspend fun login(email: String, password: String): AuthResult {
        val user = userDao.findByEmail(normalize(email)) ?: return AuthResult.UnknownEmail
        if (user.passwordHash != hashPassword(password)) return AuthResult.WrongPassword
        session.start(user.id)
        return AuthResult.Success
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val normalized = normalize(email)
        if (userDao.findByEmail(normalized) != null) return AuthResult.EmailTaken
        val id = userDao.insert(UserEntity(name = name.trim(), email = normalized, passwordHash = hashPassword(password)))
        session.start(id)
        return AuthResult.Success
    }

    suspend fun updateProfile(name: String, major: String, university: String) {
        val id = session.userId.value ?: return
        userDao.updateProfile(id, name.trim(), major.trim(), university.trim())
    }

    fun logout() = session.clear()

    private fun normalize(email: String) = email.trim().lowercase()
}

internal fun hashPassword(password: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .joinToString("") { "%02x".format(it) }
