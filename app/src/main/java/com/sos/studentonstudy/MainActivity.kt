package com.sos.studentonstudy

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sos.studentonstudy.ui.SosTheme
import com.sos.studentonstudy.ui.navigation.SosNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SosApplication).container
        val isLoggedIn = container.authRepository.isLoggedIn
        setContent {
            val dark by container.settings.darkMode.collectAsStateWithLifecycle()
            val language by container.settings.language.collectAsStateWithLifecycle()
            SideEffect {
                window.statusBarColor = (if (dark) 0xFF121212 else 0xFFF4F4F4).toInt()
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = if (dark) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            SosTheme(darkTheme = dark, language = language) {
                SosNavHost(isLoggedIn = isLoggedIn)
            }
        }
    }
}
