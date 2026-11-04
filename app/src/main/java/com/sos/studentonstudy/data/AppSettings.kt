package com.sos.studentonstudy.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** App language, as shown in Profile → Language. */
enum class AppLanguage(val code: String, val label: String) {
    English("en", "English"),
    Indonesian("id", "Bahasa Indonesia");

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: English
    }
}

/** Display preferences (dark mode, language), kept in SharedPreferences and exposed as flows. */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("sos_settings", Context.MODE_PRIVATE)
    private val _darkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK, false))
    private val _language = MutableStateFlow(AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null)))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK, enabled).apply()
        _darkMode.value = enabled
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _language.value = language
    }

    private companion object {
        const val KEY_DARK = "dark_mode"
        const val KEY_LANGUAGE = "language"
    }
}
