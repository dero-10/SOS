package com.sos.studentonstudy.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val MIN_PASSWORD_LENGTH = 6

/** Same shape Android's Patterns.EMAIL_ADDRESS accepts, without needing the Android framework (so it runs in JVM tests). */
private val EMAIL_REGEX = Regex("[a-zA-Z0-9+._%-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9-]{0,64}([.][a-zA-Z0-9][a-zA-Z0-9-]{0,25})+")

internal fun validateEmail(email: String): String? = when {
    email.isBlank() -> "Email is required"
    !EMAIL_REGEX.matches(email.trim()) -> "Enter a valid email address"
    else -> null
}

internal fun validatePassword(password: String): String? = when {
    password.isEmpty() -> "Password is required"
    password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
    else -> null
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val formError: String? = null,
    val loading: Boolean = false,
    val loggedIn: Boolean = false
)

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {
    /** The seeded demo account only exists in offline mode. */
    val showDemoHint: Boolean = !auth.isOnline

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null, formError = null) }

    fun submit() {
        val state = _uiState.value
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            val formError = when (auth.login(state.email, state.password)) {
                AuthResult.Success -> null
                AuthResult.UnknownEmail -> "No account found for this email"
                AuthResult.WrongPassword -> "Incorrect password"
                AuthResult.InvalidCredentials -> "Incorrect email or password"
                AuthResult.ConfirmEmail -> "Confirm your email first, then log in"
                AuthResult.NetworkError -> "No internet connection. Check your network and try again"
                else -> "Something went wrong on the server. Please try again"
            }
            _uiState.update { it.copy(loading = false, formError = formError, loggedIn = formError == null) }
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val formError: String? = null,
    val loading: Boolean = false,
    val registered: Boolean = false
)

class RegisterViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = null, formError = null) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, passwordError = null) }
    fun onConfirmChange(value: String) = _uiState.update { it.copy(confirmPassword = value, confirmError = null) }

    fun submit() {
        val state = _uiState.value
        val nameError = if (state.name.isBlank()) "Name is required" else null
        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmError = if (state.confirmPassword != state.password) "Passwords do not match" else null
        if (listOf(nameError, emailError, passwordError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError, confirmError = confirmError)
            }
            return
        }
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            val result = auth.register(state.name, state.email, state.password)
            _uiState.update {
                when (result) {
                    AuthResult.Success -> it.copy(loading = false, registered = true)
                    AuthResult.EmailTaken -> it.copy(loading = false, emailError = "This email is already registered")
                    AuthResult.ConfirmEmail ->
                        it.copy(loading = false, formError = "Account created. Open the link we emailed you, then log in")
                    AuthResult.NetworkError ->
                        it.copy(loading = false, formError = "No internet connection. Check your network and try again")
                    else -> it.copy(loading = false, formError = "Something went wrong on the server. Please try again")
                }
            }
        }
    }
}
