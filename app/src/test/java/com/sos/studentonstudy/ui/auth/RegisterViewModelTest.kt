package com.sos.studentonstudy.ui.auth

import com.sos.studentonstudy.data.repository.AuthResult
import com.sos.studentonstudy.testutil.FakeAuthRepository
import com.sos.studentonstudy.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private fun filled(repo: FakeAuthRepository) = RegisterViewModel(repo).apply {
        onNameChange("Ana"); onEmailChange("ana@sos.com"); onPasswordChange("secret1"); onConfirmChange("secret1")
    }

    @Test fun `successful registration marks the state registered`() {
        val repo = FakeAuthRepository(registerResult = AuthResult.Success)
        val vm = filled(repo).also { it.submit() }
        assertTrue(vm.uiState.value.registered)
        assertEquals(1, repo.registerCalls)
    }

    @Test fun `taken email shows an error on the email field`() {
        val vm = filled(FakeAuthRepository(registerResult = AuthResult.EmailTaken)).also { it.submit() }
        assertEquals("This email is already registered", vm.uiState.value.emailError)
        assertFalse(vm.uiState.value.registered)
    }

    @Test fun `mismatched passwords never reach the repository`() {
        val repo = FakeAuthRepository()
        val vm = RegisterViewModel(repo).apply {
            onNameChange("Ana"); onEmailChange("ana@sos.com"); onPasswordChange("secret1"); onConfirmChange("other11")
            submit()
        }
        assertEquals("Passwords do not match", vm.uiState.value.confirmError)
        assertEquals(0, repo.registerCalls)
    }
}
