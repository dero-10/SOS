package com.sos.studentonstudy.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.ui.formatIdr
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
    val linkedin: String = "",
    val nameError: String? = null,
    val linkedinError: String? = null
)

enum class BalanceAction(val title: String, val confirm: String) {
    Withdraw("Withdraw", "Withdraw"),
    BuyCredits("Buy Credits", "Buy")
}

/** Amount prompt for Withdraw / Buy Credits; [amount] is the digits the user typed. */
data class BalanceDialog(val action: BalanceAction, val amount: String = "", val error: String? = null)

enum class SettingDialog { Language, Display }

data class ProfileUiState(
    val user: UserEntity? = null,
    /** Non-null while the user is editing their profile. */
    val form: ProfileForm? = null,
    val message: String? = null,
    val confirmLogout: Boolean = false,
    val balanceDialog: BalanceDialog? = null,
    val settingDialog: SettingDialog? = null
)

private data class LocalState(
    val form: ProfileForm? = null,
    val message: String? = null,
    val confirmLogout: Boolean = false,
    val balanceDialog: BalanceDialog? = null,
    val settingDialog: SettingDialog? = null
)

class ProfileViewModel(private val auth: AuthRepository) : ViewModel() {
    private val local = MutableStateFlow(LocalState())

    val uiState: StateFlow<ProfileUiState> = combine(auth.currentUser, local) { user, local ->
        ProfileUiState(user, local.form, local.message, local.confirmLogout, local.balanceDialog, local.settingDialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun startEditing() {
        val user = uiState.value.user ?: return
        local.update { it.copy(form = ProfileForm(user.name, user.major, user.university, user.linkedinUrl)) }
    }

    fun cancelEditing() = local.update { it.copy(form = null) }
    fun onNameChange(value: String) = updateForm { it.copy(name = value.take(NAME_MAX), nameError = null) }
    fun onMajorChange(value: String) = updateForm { it.copy(major = value.take(FIELD_MAX)) }
    fun onUniversityChange(value: String) = updateForm { it.copy(university = value.take(FIELD_MAX)) }
    fun onLinkedinChange(value: String) = updateForm { it.copy(linkedin = value.take(URL_MAX), linkedinError = null) }

    fun save() {
        val current = local.value.form ?: return
        val linkedin = current.linkedin.trim()
        val nameError = if (current.name.isBlank()) "Name is required" else null
        val linkedinError = if (linkedin.isNotEmpty() && !linkedin.contains("linkedin.com", ignoreCase = true)) {
            "Enter a LinkedIn link, e.g. linkedin.com/in/yourname"
        } else null
        if (nameError != null || linkedinError != null) {
            updateForm { it.copy(nameError = nameError, linkedinError = linkedinError) }
            return
        }
        viewModelScope.launch {
            val url = if (linkedin.isEmpty() || linkedin.startsWith("http", ignoreCase = true)) linkedin else "https://$linkedin"
            auth.updateProfile(current.name, current.major, current.university, url)
            local.update { it.copy(form = null, message = "Profile saved") }
        }
    }

    fun openBalanceDialog(action: BalanceAction) = local.update { it.copy(balanceDialog = BalanceDialog(action)) }
    fun dismissBalanceDialog() = local.update { it.copy(balanceDialog = null) }

    fun onAmountChange(value: String) = local.update {
        it.copy(balanceDialog = it.balanceDialog?.copy(amount = value.filter(Char::isDigit).take(AMOUNT_DIGITS), error = null))
    }

    fun confirmBalance() {
        val dialog = local.value.balanceDialog ?: return
        val amount = dialog.amount.toLongOrNull()
        val balance = uiState.value.user?.balance ?: 0L
        val error = when {
            amount == null || amount <= 0 -> "Enter an amount"
            dialog.action == BalanceAction.Withdraw && amount > balance -> "You only have ${formatIdr(balance)}"
            else -> null
        }
        if (error != null || amount == null) {
            local.update { it.copy(balanceDialog = dialog.copy(error = error)) }
            return
        }
        viewModelScope.launch {
            val done = when (dialog.action) {
                BalanceAction.Withdraw -> auth.withdraw(amount)
                BalanceAction.BuyCredits -> auth.topUp(amount)
            }
            local.update {
                if (done) {
                    val text = if (dialog.action == BalanceAction.Withdraw) "${formatIdr(amount)} withdrawn" else "${formatIdr(amount)} added to your balance"
                    it.copy(balanceDialog = null, message = text)
                } else {
                    it.copy(balanceDialog = dialog.copy(error = "Balance changed, please try again"))
                }
            }
        }
    }

    fun openSettingDialog(value: SettingDialog) = local.update { it.copy(settingDialog = value) }
    fun dismissSettingDialog() = local.update { it.copy(settingDialog = null) }

    fun showMessage(value: String) = local.update { it.copy(message = value) }
    fun messageShown() = local.update { it.copy(message = null) }
    fun requestLogout() = local.update { it.copy(confirmLogout = true) }
    fun cancelLogout() = local.update { it.copy(confirmLogout = false) }

    fun logout() {
        local.update { it.copy(confirmLogout = false) }
        auth.logout()
    }

    private fun updateForm(transform: (ProfileForm) -> ProfileForm) = local.update { state ->
        state.copy(form = state.form?.let(transform))
    }

    private companion object {
        const val NAME_MAX = 50
        const val FIELD_MAX = 60
        const val URL_MAX = 120
        const val AMOUNT_DIGITS = 8
    }
}
