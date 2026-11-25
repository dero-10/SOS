package com.sos.studentonstudy.ui.profile

import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.testutil.FakeAppSettings
import com.sos.studentonstudy.testutil.FakeAuthRepository
import com.sos.studentonstudy.testutil.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private val settings = FakeAppSettings()

    private fun user(balance: Long = 100_000L) = UserEntity(
        id = 1, name = "Farrell", email = "farrell@example.com", passwordHash = "x",
        major = "Informatics", university = "UPH", linkedinUrl = "", balance = balance
    )

    /** Builds the ViewModel and keeps uiState subscribed (it uses WhileSubscribed). */
    private fun TestScope.vm(user: UserEntity?): ProfileViewModel {
        val auth = FakeAuthRepository().apply { this.user.value = user }
        val vm = ProfileViewModel(auth, settings)
        backgroundScope.launch(mainRule.dispatcher) { vm.uiState.collect {} }
        return vm
    }

    @Test fun `saving a valid profile closes the form and shows a message`() = runTest {
        val vm = vm(user())
        vm.startEditing()
        vm.onNameChange("Farrell Tamaela")
        vm.onLinkedinChange("linkedin.com/in/farrell")
        vm.save()

        val state = vm.uiState.value
        assertNull(state.form)
        assertEquals("Profile saved", state.message)
    }

    @Test fun `blank name is rejected and the form stays open`() = runTest {
        val vm = vm(user())
        vm.startEditing()
        vm.onNameChange("   ")
        vm.save()

        val form = vm.uiState.value.form
        assertNotNull(form)
        assertEquals("Name is required", form!!.nameError)
        assertNull(vm.uiState.value.message)
    }

    @Test fun `non LinkedIn link is rejected`() = runTest {
        val vm = vm(user())
        vm.startEditing()
        vm.onLinkedinChange("github.com/farrell")
        vm.save()

        val form = vm.uiState.value.form
        assertNotNull(form)
        assertNull(form!!.nameError)
        assertEquals("Enter a LinkedIn link, e.g. linkedin.com/in/yourname", form.linkedinError)
    }

    @Test fun `withdrawing more than the balance shows an error`() = runTest {
        val vm = vm(user(balance = 100_000L))
        vm.openBalanceDialog(BalanceAction.Withdraw)
        vm.onAmountChange("150000")
        vm.confirmBalance()

        val dialog = vm.uiState.value.balanceDialog
        assertNotNull(dialog)
        assertEquals("You only have IDR 100,000", dialog!!.error)
        assertNull(vm.uiState.value.message)
    }

    @Test fun `form is empty until editing starts`() = runTest {
        val vm = vm(user())
        assertNull(vm.uiState.value.form)

        // Field changes and save are ignored while no form is open.
        vm.onNameChange("Someone")
        vm.save()
        assertNull(vm.uiState.value.form)
        assertNull(vm.uiState.value.message)
    }

    @Test fun `editing cannot start without a logged in user`() = runTest {
        val vm = vm(user = null)
        vm.startEditing()
        assertNull(vm.uiState.value.form)
    }

    @Test fun `dark mode toggle goes through settings`() = runTest {
        val vm = vm(user())
        vm.setDarkMode(true)
        assertTrue(vm.darkMode.value)
        assertTrue(settings.darkMode.value)
    }
}
