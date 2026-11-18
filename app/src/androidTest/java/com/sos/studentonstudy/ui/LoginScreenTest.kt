package com.sos.studentonstudy.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.sos.studentonstudy.data.repository.AuthResult
import com.sos.studentonstudy.testutil.FakeAuthRepository
import com.sos.studentonstudy.ui.auth.LoginScreen
import com.sos.studentonstudy.ui.auth.LoginViewModel
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {
    @get:Rule val compose = createComposeRule()

    private fun show(repo: FakeAuthRepository) {
        compose.setContent { SosTheme { LoginScreen(onLoggedIn = {}, onRegister = {}, viewModel = LoginViewModel(repo)) } }
    }

    private fun tapLogin() = compose.onAllNodesWithText("Login").onLast().performClick()

    @Test fun emptySubmitShowsValidationErrors() {
        show(FakeAuthRepository())
        tapLogin()
        compose.onNodeWithText("Email is required").assertIsDisplayed()
        compose.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test fun wrongPasswordShowsServerError() {
        show(FakeAuthRepository(loginResult = AuthResult.WrongPassword))
        compose.onNodeWithText("Enter your email").performTextInput("demo@sos.com")
        compose.onNodeWithText("Enter your password").performTextInput("badpass1")
        tapLogin()
        compose.onNodeWithText("Incorrect password").assertIsDisplayed()
    }
}
