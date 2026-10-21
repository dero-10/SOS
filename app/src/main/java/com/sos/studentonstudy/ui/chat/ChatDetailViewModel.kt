package com.sos.studentonstudy.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.ChatMessageEntity
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.repository.ChatRepository
import com.sos.studentonstudy.data.repository.HelperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class Draft(val text: String = "", val attachmentName: String? = null)

data class ChatDetailUiState(
    val helper: HelperEntity? = null,
    /** Oldest first. */
    val messages: List<ChatMessageEntity> = emptyList(),
    val draftText: String = "",
    val draftAttachment: String? = null,
    val loading: Boolean = true
)

class ChatDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val chat: ChatRepository,
    helpers: HelperRepository
) : ViewModel() {
    private val helperId: Long = savedStateHandle.get<Long>(HELPER_ID_ARG) ?: -1L
    private val draft = MutableStateFlow(Draft())

    val uiState: StateFlow<ChatDetailUiState> = combine(
        helpers.helper(helperId),
        chat.messages(helperId),
        draft
    ) { helper, messages, draft ->
        ChatDetailUiState(helper, messages, draft.text, draft.attachmentName, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatDetailUiState())

    fun onTextChange(value: String) = draft.update { it.copy(text = value.take(MESSAGE_MAX)) }
    fun onAttachmentChange(name: String?) = draft.update { it.copy(attachmentName = name) }

    fun send() {
        val current = draft.value
        if (current.text.isBlank() && current.attachmentName == null) return
        draft.value = Draft()
        viewModelScope.launch { chat.send(helperId, current.text, current.attachmentName) }
    }

    companion object {
        const val HELPER_ID_ARG = "helperId"
        private const val MESSAGE_MAX = 500
    }
}
