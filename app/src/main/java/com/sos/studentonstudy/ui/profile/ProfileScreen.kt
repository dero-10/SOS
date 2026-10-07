package com.sos.studentonstudy.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.ui.BackTopBar
import com.sos.studentonstudy.ui.ConfirmDialog
import com.sos.studentonstudy.ui.InitialAvatar
import com.sos.studentonstudy.ui.SosButton
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosRow
import com.sos.studentonstudy.ui.SosTeal
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosTextField
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.formatIdr

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    BackHandler(enabled = state.form != null, onBack = viewModel::cancelEditing)

    Box(modifier = Modifier.fillMaxSize()) {
        val form = state.form
        if (form == null) {
            ProfileContent(
                user = state.user,
                onEdit = viewModel::startEditing,
                onComingSoon = { viewModel.showMessage("$it is coming soon") },
                onLogout = viewModel::requestLogout
            )
        } else {
            EditProfileContent(form, viewModel)
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (state.confirmLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You will need to log in again to access your requests.",
            confirmText = "Logout",
            onConfirm = {
                viewModel.logout()
                onLoggedOut()
            },
            onDismiss = viewModel::cancelLogout
        )
    }
}

@Composable
private fun ProfileContent(user: UserEntity?, onEdit: () -> Unit, onComingSoon: (String) -> Unit, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // White sheet behind the content, starting halfway down the avatar like the Figma frame.
        Box(
            Modifier
                .padding(top = 188.dp)
                .fillMaxWidth()
                .height(900.dp)
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        )
        Column(modifier = Modifier.padding(top = 140.dp)) {
            Row(modifier = Modifier.padding(horizontal = 32.dp), verticalAlignment = Alignment.Top) {
                InitialAvatar(user?.name.orEmpty(), 80.dp)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user?.name.orEmpty(), color = SosText, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(user?.email.orEmpty(), color = SosText, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val education = listOf(user?.major.orEmpty(), user?.university.orEmpty()).filter { it.isNotBlank() }.joinToString(", ")
                    if (education.isNotEmpty()) Text(education, color = SosText, fontSize = 9.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Edit Profile",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(SosPurple, CircleShape)
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(48.dp))
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SosTeal,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("Your Balance", color = SosText, fontSize = 12.sp)
                    Text(formatIdr(user?.balance ?: 0), color = SosText, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BalanceButton("Withdraw", Icons.Outlined.AccountBalance, Modifier.weight(1f)) { onComingSoon("Withdraw") }
                        BalanceButton("Buy Credits", Icons.Outlined.Payments, Modifier.weight(1f)) { onComingSoon("Buy Credits") }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingRow(Icons.Outlined.Language, "Language", "English") { onComingSoon("Language settings") }
                SettingRow(Icons.Outlined.PhoneAndroid, "Display", "Light mode") { onComingSoon("Display settings") }
                SettingRow(Icons.AutoMirrored.Outlined.Logout, "Logout", null, onLogout)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BalanceButton(text: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SosPurple)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
    }
}

@Composable
private fun SettingRow(icon: ImageVector, label: String, value: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SosRow, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SosText)
        Spacer(Modifier.width(18.dp))
        Text(label, color = SosText, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (value != null) Text(value, color = SosMuted, fontSize = 14.sp)
    }
}

@Composable
private fun EditProfileContent(form: ProfileForm, viewModel: ProfileViewModel) {
    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        BackTopBar(title = "Edit Profile", onBack = viewModel::cancelEditing)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SosTextField(
                label = "Full name",
                value = form.name,
                onValueChange = viewModel::onNameChange,
                placeholder = "Enter your name",
                error = form.nameError,
                leadingIcon = Icons.Outlined.Person,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            SosTextField(
                label = "Major",
                value = form.major,
                onValueChange = viewModel::onMajorChange,
                placeholder = "e.g. Product Design",
                leadingIcon = Icons.Outlined.School,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            SosTextField(
                label = "University",
                value = form.university,
                onValueChange = viewModel::onUniversityChange,
                placeholder = "e.g. Universitas Pelita Harapan",
                leadingIcon = Icons.Outlined.AccountBalance,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
        }
        SosButton("Save", viewModel::save, modifier = Modifier.padding(horizontal = 40.dp, vertical = 20.dp))
    }
}
