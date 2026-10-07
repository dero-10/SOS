package com.sos.studentonstudy.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.ui.SosButton
import com.sos.studentonstudy.ui.SosTextField
import com.sos.studentonstudy.ui.SosViewModelFactory

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.registered) { if (state.registered) onRegistered() }

    AuthScaffold(title = "Register", subtitle = "Create your Student on Study account") {
        SosTextField(
            label = "Full name",
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = "Enter your name",
            error = state.nameError,
            leadingIcon = Icons.Outlined.Person,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(20.dp))
        SosTextField(
            label = "Email",
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Enter your email",
            error = state.emailError,
            leadingIcon = Icons.Outlined.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(20.dp))
        PasswordField("Password", state.password, viewModel::onPasswordChange, state.passwordError, ImeAction.Next)
        Spacer(Modifier.height(20.dp))
        PasswordField("Confirm password", state.confirmPassword, viewModel::onConfirmChange, state.confirmError, ImeAction.Done, viewModel::submit)
        Spacer(Modifier.height(30.dp))
        SosButton("Create account", viewModel::submit, loading = state.loading)
        AuthSwitch("Already have an account?", "Login", onLogin)
    }
}
