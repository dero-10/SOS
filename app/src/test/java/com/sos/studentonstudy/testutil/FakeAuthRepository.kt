package com.sos.studentonstudy.testutil

import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.data.repository.AuthResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory AuthRepository for tests: set [loginResult] / [registerResult] to script the outcome. */
class FakeAuthRepository(
    override val isOnline: Boolean = false,
    var loginResult: AuthResult = AuthResult.Success,
    var registerResult: AuthResult = AuthResult.Success
) : AuthRepository {
    val user = MutableStateFlow<UserEntity?>(null)
    var loginCalls = 0
        private set
    var registerCalls = 0
        private set
    var lastLoginEmail: String? = null
        private set

    override var isLoggedIn: Boolean = false
        private set
    override val currentUser: Flow<UserEntity?> = user

    override suspend fun login(email: String, password: String): AuthResult {
        loginCalls++
        lastLoginEmail = email
        if (loginResult == AuthResult.Success) isLoggedIn = true
        return loginResult
    }

    override suspend fun register(name: String, email: String, password: String): AuthResult {
        registerCalls++
        return registerResult
    }

    override suspend fun updateProfile(name: String, major: String, university: String, linkedinUrl: String) = true
    override suspend fun topUp(amount: Long) = true
    override suspend fun withdraw(amount: Long) = true
    override suspend fun syncSession() = Unit
    override fun logout() { isLoggedIn = false }
}
