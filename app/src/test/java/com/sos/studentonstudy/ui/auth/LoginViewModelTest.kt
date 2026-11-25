package com.sos.studentonstudy.ui.auth

import com.sos.studentonstudy.data.repository.AuthResult
import com.sos.studentonstudy.testutil.FakeAuthRepository
import com.sos.studentonstudy.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test fun `valid credentials log the user in`() {
        val repo = FakeAuthRepository(loginResult = AuthResult.Success)
        val vm = LoginViewModel(repo)
        vm.onEmailChange("demo@sos.com"); vm.onPasswordChange("password123")
        vm.submit()
        val state = vm.uiState.value
        assertTrue(state.loggedIn)
        assertNull(state.formError)
        assertFalse(state.loading)
        assertEquals("demo@sos.com", repo.lastLoginEmail)
    }

    @Test fun `wrong password shows an error and stays logged out`() {
        val vm = LoginViewModel(FakeAuthRepository(loginResult = AuthResult.WrongPassword))
        vm.onEmailChange("demo@sos.com"); vm.onPasswordChange("badpass1")
        vm.submit()
        assertEquals("Incorrect password", vm.uiState.value.formError)
        assertFalse(vm.uiState.value.loggedIn)
    }

    @Test fun `network failure shows the offline message`() {
        val vm = LoginViewModel(FakeAuthRepository(loginResult = AuthResult.NetworkError))
        vm.onEmailChange("demo@sos.com"); vm.onPasswordChange("password123")
        vm.submit()
        assertEquals("No internet connection. Check your network and try again", vm.uiState.value.formError)
    }

    @Test fun `empty fields are rejected without calling the repository`() {
        val repo = FakeAuthRepository()
        val vm = LoginViewModel(repo)
        vm.submit()
        assertEquals("Email is required", vm.uiState.value.emailError)
        assertEquals("Password is required", vm.uiState.value.passwordError)
        assertEquals(0, repo.loginCalls)
    }

    @Test fun `malformed email and short password are rejected`() {
        val repo = FakeAuthRepository()
        val vm = LoginViewModel(repo)
        vm.onEmailChange("not-an-email"); vm.onPasswordChange("123")
        vm.submit()
        assertEquals("Enter a valid email address", vm.uiState.value.emailError)
        assertEquals("Password must be at least 6 characters", vm.uiState.value.passwordError)
        assertEquals(0, repo.loginCalls)
    }
}
