package com.sos.studentonstudy.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.ui.SosButton
import com.sos.studentonstudy.ui.SosError
import com.sos.studentonstudy.ui.SosLogo
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosTextField
import com.sos.studentonstudy.ui.SosViewModelFactory

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.loggedIn) { if (state.loggedIn) onLoggedIn() }

    AuthScaffold(title = "Login", subtitle = "Welcome back to Student on Study") {
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
        PasswordField(
            label = "Password",
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            error = state.passwordError,
            imeAction = ImeAction.Done,
            onDone = viewModel::submit
        )
        state.formError?.let {
            Text(it, color = SosError, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 16.dp))
        }
        Spacer(Modifier.height(30.dp))
        SosButton("Login", viewModel::submit, loading = state.loading)
        AuthSwitch("Don't have an account?", "Register", onRegister)
        Text("Demo account: demo@sos.com / password123", color = SosMuted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
internal fun AuthScaffold(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))
            SosLogo()
            Spacer(Modifier.height(48.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(title, color = SosText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(subtitle, color = SosMuted, fontSize = 14.sp)
                Spacer(Modifier.height(32.dp))
                content()
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
internal fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    imeAction: ImeAction,
    onDone: () -> Unit = {}
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    SosTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = "Enter your password",
        error = error,
        leadingIcon = Icons.Outlined.Lock,
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (showPassword) "Hide password" else "Show password",
                    tint = SosMuted
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )
}

@Composable
internal fun AuthSwitch(question: String, action: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(question, color = SosMuted, fontSize = 13.sp)
        TextButton(onClick = onClick) { Text(action, color = SosPurple, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
    }
}
