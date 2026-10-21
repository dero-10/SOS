package com.sos.studentonstudy.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.local.ChatPreview
import com.sos.studentonstudy.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ChatListUiState(
    val query: String = "",
    val chats: List<ChatPreview> = emptyList(),
    /** True when the user has any conversation at all, even if the search hides them. */
    val hasChats: Boolean = false,
    val loading: Boolean = true
)

class ChatListViewModel(repository: ChatRepository) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<ChatListUiState> = combine(repository.previews, query) { previews, query ->
        ChatListUiState(
            query = query,
            chats = previews.filter { query.isBlank() || it.helperName.contains(query.trim(), ignoreCase = true) },
            hasChats = previews.isNotEmpty(),
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatListUiState())

    fun setQuery(value: String) { query.value = value }
}
