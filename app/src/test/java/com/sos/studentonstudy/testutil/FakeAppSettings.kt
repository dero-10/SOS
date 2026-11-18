package com.sos.studentonstudy.testutil

import com.sos.studentonstudy.data.AppLanguage
import com.sos.studentonstudy.data.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory AppSettings for tests: no SharedPreferences, no Context. */
class FakeAppSettings(
    darkMode: Boolean = false,
    language: AppLanguage = AppLanguage.English
) : AppSettings {
    private val _darkMode = MutableStateFlow(darkMode)
    private val _language = MutableStateFlow(language)
    override val darkMode: StateFlow<Boolean> = _darkMode
    override val language: StateFlow<AppLanguage> = _language

    override fun setDarkMode(enabled: Boolean) { _darkMode.value = enabled }
    override fun setLanguage(language: AppLanguage) { _language.value = language }
}
