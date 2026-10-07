package com.sos.studentonstudy

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sos.studentonstudy.ui.SosTheme
import com.sos.studentonstudy.ui.navigation.SosNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFFF4F4F4.toInt()
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        val isLoggedIn = (application as SosApplication).container.authRepository.isLoggedIn
        setContent {
            SosTheme {
                SosNavHost(isLoggedIn = isLoggedIn)
            }
        }
    }
}
