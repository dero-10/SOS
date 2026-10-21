package com.sos.studentonstudy.ui.helper

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.HelpHistoryEntity
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.repository.HelperRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HelperProfileUiState(
    val helper: HelperEntity? = null,
    val recentHelps: List<HelpHistoryEntity> = emptyList(),
    val loading: Boolean = true
)

class HelperProfileViewModel(savedStateHandle: SavedStateHandle, repository: HelperRepository) : ViewModel() {
    private val helperId: Long = savedStateHandle.get<Long>(HELPER_ID_ARG) ?: -1L

    val uiState: StateFlow<HelperProfileUiState> = combine(
        repository.helper(helperId),
        repository.recentHelps(helperId)
    ) { helper, history ->
        HelperProfileUiState(helper, history, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HelperProfileUiState())

    companion object {
        const val HELPER_ID_ARG = "helperId"
    }
}
