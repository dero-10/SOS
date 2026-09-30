package com.sos.studentonstudy

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.sos.studentonstudy.ui.DashboardScreen
import com.sos.studentonstudy.ui.LoginScreen
import com.sos.studentonstudy.ui.MainMenuScreen
import com.sos.studentonstudy.ui.SosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.WHITE
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setContent {
            SosTheme {
                SosApp()
            }
        }
    }
}

private enum class Screen { Login, MainMenu, Dashboard }

@Composable
private fun SosApp() {
    var screen by rememberSaveable { mutableStateOf(Screen.Login) }
    var studentName by rememberSaveable { mutableStateOf("Student") }

    BackHandler(enabled = screen != Screen.Login) {
        screen = when (screen) {
            Screen.Dashboard -> Screen.MainMenu
            Screen.MainMenu -> Screen.Login
            Screen.Login -> Screen.Login
        }
    }

    when (screen) {
        Screen.Login -> LoginScreen { name ->
            studentName = name
            screen = Screen.MainMenu
        }
        Screen.MainMenu -> MainMenuScreen(
            studentName = studentName,
            onDashboard = { screen = Screen.Dashboard },
            onLogout = { screen = Screen.Login }
        )
        Screen.Dashboard -> DashboardScreen(
            studentName = studentName,
            onMenu = { screen = Screen.MainMenu }
        )
    }
}
