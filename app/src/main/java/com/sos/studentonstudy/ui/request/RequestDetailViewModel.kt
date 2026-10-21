package com.sos.studentonstudy.ui.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.ReplyWithAuthor
import com.sos.studentonstudy.data.local.RequestWithMeta
import com.sos.studentonstudy.data.repository.ReplyRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class ReplyDraft(val text: String = "", val attachmentName: String? = null)

data class RequestDetailUiState(
    val item: RequestWithMeta? = null,
    /** True when the logged-in user wrote the request; own requests get the teal header and no reply bar. */
    val isOwn: Boolean = false,
    val replies: List<ReplyWithAuthor> = emptyList(),
    val replyText: String = "",
    val replyAttachment: String? = null,
    val loading: Boolean = true
) {
    val canSend: Boolean get() = replyText.isNotBlank()
}

class RequestDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val requests: RequestRepository,
    private val replies: ReplyRepository
) : ViewModel() {
    private val requestId: Long = savedStateHandle.get<Long>(REQUEST_ID_ARG) ?: -1L
    private val draft = MutableStateFlow(ReplyDraft())

    val uiState: StateFlow<RequestDetailUiState> = combine(
        requests.observe(requestId),
        replies.replies(requestId),
        draft
    ) { item, replyList, draft ->
        RequestDetailUiState(
            item = item,
            isOwn = item != null && item.request.userId == requests.currentUserId,
            replies = replyList,
            replyText = draft.text,
            replyAttachment = draft.attachmentName,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RequestDetailUiState())

    fun onReplyChange(value: String) = draft.update { it.copy(text = value.take(REPLY_MAX)) }
    fun onAttachmentChange(name: String?) = draft.update { it.copy(attachmentName = name) }

    fun postReply() {
        val current = draft.value
        if (current.text.isBlank()) return
        draft.value = ReplyDraft()
        viewModelScope.launch { replies.post(requestId, current.text, current.attachmentName) }
    }

    companion object {
        const val REQUEST_ID_ARG = "requestId"
        private const val REPLY_MAX = 300
    }
}
