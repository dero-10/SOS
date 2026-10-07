package com.sos.studentonstudy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import com.sos.studentonstudy.ui.daysFromToday
import com.sos.studentonstudy.ui.isUrgent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val name: String = "",
    val balance: Long = 0,
    val total: Int = 0,
    val urgent: Int = 0,
    val dueThisWeek: Int = 0,
    val recent: List<RequestEntity> = emptyList(),
    val loading: Boolean = true
)

class DashboardViewModel(auth: AuthRepository, requests: RequestRepository) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = combine(auth.currentUser, requests.requests) { user, list ->
        val weekEnd = daysFromToday(7)
        DashboardUiState(
            name = user?.name.orEmpty(),
            balance = user?.balance ?: 0,
            total = list.size,
            urgent = list.count { isUrgent(it.deadline) },
            dueThisWeek = list.count { it.deadline <= weekEnd },
            recent = list.sortedByDescending { it.createdAt }.take(RECENT_LIMIT),
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private companion object {
        const val RECENT_LIMIT = 3
    }
}
