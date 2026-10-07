package com.sos.studentonstudy.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileForm(
    val name: String = "",
    val major: String = "",
    val university: String = "",
    val nameError: String? = null
)

data class ProfileUiState(
    val user: UserEntity? = null,
    /** Non-null while the user is editing their profile. */
    val form: ProfileForm? = null,
    val message: String? = null,
    val confirmLogout: Boolean = false
)

class ProfileViewModel(private val auth: AuthRepository) : ViewModel() {
    private val form = MutableStateFlow<ProfileForm?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val confirmLogout = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> = combine(auth.currentUser, form, message, confirmLogout) { user, form, message, confirmLogout ->
        ProfileUiState(user, form, message, confirmLogout)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun startEditing() {
        val user = uiState.value.user ?: return
        form.value = ProfileForm(user.name, user.major, user.university)
    }

    fun cancelEditing() { form.value = null }
    fun onNameChange(value: String) = form.update { it?.copy(name = value.take(NAME_MAX), nameError = null) }
    fun onMajorChange(value: String) = form.update { it?.copy(major = value.take(FIELD_MAX)) }
    fun onUniversityChange(value: String) = form.update { it?.copy(university = value.take(FIELD_MAX)) }

    fun save() {
        val current = form.value ?: return
        if (current.name.isBlank()) {
            form.value = current.copy(nameError = "Name is required")
            return
        }
        viewModelScope.launch {
            auth.updateProfile(current.name, current.major, current.university)
            form.value = null
            message.value = "Profile saved"
        }
    }

    fun showMessage(value: String) { message.value = value }
    fun messageShown() { message.value = null }
    fun requestLogout() { confirmLogout.value = true }
    fun cancelLogout() { confirmLogout.value = false }

    fun logout() {
        confirmLogout.value = false
        auth.logout()
    }

    private companion object {
        const val NAME_MAX = 50
        const val FIELD_MAX = 60
    }
}
