package com.sos.studentonstudy.ui.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.EstimatedTime
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.RequestTags
import com.sos.studentonstudy.data.local.tagList
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import com.sos.studentonstudy.ui.daysFromToday
import com.sos.studentonstudy.ui.formatIdr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Quick deadline choices from the Figma form; the last one opens a date picker. */
enum class DeadlineOption(val label: String, val days: Int?) {
    Today("Today", 0), Tomorrow("Tomorrow", 1), ThreeDays("3 days", 3), OneWeek("1 week", 7), Custom("Custom date", null)
}

data class RequestFormUiState(
    val isEdit: Boolean = false,
    val title: String = "",
    val details: String = "",
    val attachmentName: String? = null,
    val estimatedTime: Int = EstimatedTime.FAST,
    val deadline: Long? = null,
    val deadlineOption: DeadlineOption? = null,
    val budget: Long = RequestFormViewModel.DEFAULT_BUDGET,
    val tags: Set<String> = emptySet(),
    val titleError: String? = null,
    val detailsError: String? = null,
    val deadlineError: String? = null,
    val tagsError: String? = null,
    val budgetError: String? = null,
    val confirmDelete: Boolean = false,
    val loading: Boolean = false,
    val finished: Boolean = false
)

class RequestFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RequestRepository,
    private val auth: AuthRepository
) : ViewModel() {
    private val requestId: Long = savedStateHandle.get<Long>(REQUEST_ID_ARG) ?: NEW_REQUEST_ID
    private var original: RequestEntity? = null

    private val _uiState = MutableStateFlow(RequestFormUiState(isEdit = requestId != NEW_REQUEST_ID, loading = requestId != NEW_REQUEST_ID))
    val uiState: StateFlow<RequestFormUiState> = _uiState.asStateFlow()

    init {
        if (requestId != NEW_REQUEST_ID) {
            viewModelScope.launch {
                val request = repository.get(requestId)
                original = request
                _uiState.update {
                    if (request == null) it.copy(loading = false, finished = true)
                    else it.copy(
                        title = request.title,
                        details = request.details,
                        attachmentName = request.attachmentName,
                        estimatedTime = request.estimatedTime,
                        deadline = request.deadline,
                        deadlineOption = DeadlineOption.entries.firstOrNull { o -> o.days != null && daysFromToday(o.days) == request.deadline }
                            ?: DeadlineOption.Custom,
                        budget = request.budget,
                        tags = request.tagList().toSet(),
                        loading = false
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value.take(TITLE_MAX), titleError = null) }
    fun onDetailsChange(value: String) = _uiState.update { it.copy(details = value.take(DETAILS_MAX), detailsError = null) }
    fun onAttachmentChange(name: String?) = _uiState.update { it.copy(attachmentName = name) }
    fun onEstimatedTimeChange(value: Int) = _uiState.update { it.copy(estimatedTime = value) }

    fun onDeadlineOption(option: DeadlineOption) {
        val days = option.days ?: return
        _uiState.update { it.copy(deadline = daysFromToday(days), deadlineOption = option, deadlineError = null) }
    }

    fun onCustomDeadline(millis: Long) = _uiState.update {
        it.copy(deadline = millis, deadlineOption = DeadlineOption.Custom, deadlineError = null)
    }

    fun changeBudget(up: Boolean) = _uiState.update {
        val next = if (up) it.budget + BUDGET_STEP else it.budget - BUDGET_STEP
        it.copy(budget = next.coerceAtLeast(BUDGET_STEP), budgetError = null)
    }

    fun toggleTag(tag: String) = _uiState.update {
        it.copy(tags = if (tag in it.tags) it.tags - tag else it.tags + tag, tagsError = null)
    }

    fun requestDelete() = _uiState.update { it.copy(confirmDelete = true) }
    fun cancelDelete() = _uiState.update { it.copy(confirmDelete = false) }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            val balance = auth.currentUser.first()?.balance ?: 0L
            val titleError = when {
                state.title.isBlank() -> "Title is required"
                state.title.trim().length < 5 -> "Title must be at least 5 characters"
                else -> null
            }
            val detailsError = when {
                state.details.isBlank() -> "Request details are required"
                state.details.trim().length < 10 -> "Describe your request in at least 10 characters"
                else -> null
            }
            val deadlineError = if (state.deadline == null) "Choose a deadline" else null
            val tagsError = if (state.tags.isEmpty()) "Pick at least one tag" else null
            val budgetError = if (state.budget > balance) "Budget is higher than your balance (${formatIdr(balance)})" else null
            if (listOf(titleError, detailsError, deadlineError, tagsError, budgetError).any { it != null }) {
                _uiState.update {
                    it.copy(titleError = titleError, detailsError = detailsError, deadlineError = deadlineError, tagsError = tagsError, budgetError = budgetError)
                }
                return@launch
            }
            val base = original ?: RequestEntity(userId = 0, title = "", details = "", tags = "", estimatedTime = 0, deadline = 0, budget = 0)
            repository.save(
                base.copy(
                    title = state.title.trim(),
                    details = state.details.trim(),
                    attachmentName = state.attachmentName,
                    estimatedTime = state.estimatedTime,
                    deadline = state.deadline!!,
                    budget = state.budget,
                    // Keep the Figma tag order rather than the order the user tapped them.
                    tags = RequestTags.all.filter { it in state.tags }.joinToString(",")
                )
            )
            _uiState.update { it.copy(finished = true) }
        }
    }

    fun confirmDelete() {
        val request = original ?: return
        _uiState.update { it.copy(confirmDelete = false) }
        viewModelScope.launch {
            repository.delete(request)
            _uiState.update { it.copy(finished = true) }
        }
    }

    companion object {
        const val REQUEST_ID_ARG = "requestId"
        const val NEW_REQUEST_ID = -1L
        const val DEFAULT_BUDGET = 50_000L
        private const val BUDGET_STEP = 5_000L
        private const val TITLE_MAX = 60
        private const val DETAILS_MAX = 500
    }
}
