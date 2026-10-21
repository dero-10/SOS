package com.sos.studentonstudy.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.data.local.ChatMessageEntity
import com.sos.studentonstudy.ui.AttachmentChip
import com.sos.studentonstudy.ui.EmptyState
import com.sos.studentonstudy.ui.MessageField
import com.sos.studentonstudy.ui.PersonAvatar
import com.sos.studentonstudy.ui.SosBackground
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosStroke
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.rememberAttachmentPicker

/** Chat detail from Figma: helper header, message bubbles pinned to the bottom, and the message bar. */
@Composable
fun ChatDetailScreen(
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pickFile = rememberAttachmentPicker { viewModel.onAttachmentChange(it.name) }
    val listState = rememberLazyListState()
    // The list is reversed (index 0 is the newest message), so scrolling to 0 shows the latest bubble.
    LaunchedEffect(state.messages.size) { listState.scrollToItem(0) }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SosPurple)
            }
            PersonAvatar(36.dp)
            Spacer(Modifier.width(12.dp))
            Text(state.helper?.name.orEmpty(), color = SosText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        when {
            state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SosPurple)
            }
            state.messages.isEmpty() -> EmptyState("No messages yet", "Say hello to ${state.helper?.name ?: "your helper"}.", Modifier.weight(1f))
            else -> LazyColumn(
                state = listState,
                reverseLayout = true,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages.asReversed(), key = { it.id }) { MessageBubble(it) }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().background(SosBackground)) {
            state.draftAttachment?.let {
                AttachmentChip(it, modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp), onRemove = { viewModel.onAttachmentChange(null) })
            }
            MessageField(
                value = state.draftText,
                onValueChange = viewModel::onTextChange,
                placeholder = "Message",
                onAttach = pickFile,
                onSend = viewModel::send
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageEntity) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (message.fromMe) Alignment.End else Alignment.Start) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(if (message.fromMe) SosBackground else Color.White, RoundedCornerShape(20.dp))
                .border(1.dp, SosStroke, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            if (message.text.isNotBlank()) {
                Text(message.text, color = SosText, fontSize = 15.sp)
            }
            message.attachmentName?.let {
                if (message.text.isNotBlank()) Spacer(Modifier.height(12.dp))
                AttachmentChip(it)
            }
        }
        if (message.isRequest) {
            Text(
                "Request has been sent.",
                color = SosMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(top = 6.dp, end = 4.dp)
            )
        }
    }
}
