package com.sos.studentonstudy.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.data.local.ChatPreview
import com.sos.studentonstudy.ui.EmptyState
import com.sos.studentonstudy.ui.PersonAvatar
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosSearchBar
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.formatTimeAgo

/** Chat tab: search plus one row per conversation, newest first. */
@Composable
fun ChatListScreen(
    onOpenChat: (Long) -> Unit,
    viewModel: ChatListViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { SosSearchBar(state.query, viewModel::setQuery) }
        when {
            state.loading -> item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SosPurple)
                }
            }
            !state.hasChats -> item {
                EmptyState("No conversations yet", "Open a helper's profile and tap Chat Helper to start one.")
            }
            state.chats.isEmpty() -> item { EmptyState("No matching conversations", "Try a different name.") }
            else -> items(state.chats, key = { it.helperId }) { chat ->
                ChatRow(chat, onClick = { onOpenChat(chat.helperId) })
            }
        }
    }
}

@Composable
private fun ChatRow(chat: ChatPreview, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PersonAvatar(54.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(chat.helperName, color = SosText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text(formatTimeAgo(chat.createdAt), color = SosMuted, fontSize = 13.sp)
                }
                Spacer(Modifier.height(2.dp))
                Text(chat.previewText(), color = SosMuted, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun ChatPreview.previewText(): String = when {
    isRequest -> "You sent your request"
    lastText.isBlank() -> "Sent an attachment"
    else -> lastText
}
