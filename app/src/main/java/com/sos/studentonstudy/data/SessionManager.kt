package com.sos.studentonstudy.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persists the logged-in user id so the session survives app restarts. */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("sos_session", Context.MODE_PRIVATE)
    private val _userId = MutableStateFlow(prefs.getLong(KEY_USER_ID, NO_USER).takeIf { it != NO_USER })
    val userId: StateFlow<Long?> = _userId.asStateFlow()

    fun start(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
        _userId.value = userId
    }

    fun clear() {
        prefs.edit().remove(KEY_USER_ID).apply()
        _userId.value = null
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val NO_USER = -1L
    }
}
