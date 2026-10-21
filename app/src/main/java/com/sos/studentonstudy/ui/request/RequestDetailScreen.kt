package com.sos.studentonstudy.ui.request

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.sos.studentonstudy.data.local.EstimatedTime
import com.sos.studentonstudy.data.local.ReplyWithAuthor
import com.sos.studentonstudy.data.local.RequestWithMeta
import com.sos.studentonstudy.data.local.tagList
import com.sos.studentonstudy.ui.AttachmentChip
import com.sos.studentonstudy.ui.BackTopBar
import com.sos.studentonstudy.ui.InitialAvatar
import com.sos.studentonstudy.ui.MessageField
import com.sos.studentonstudy.ui.PersonAvatar
import com.sos.studentonstudy.ui.SosAvatarGray
import com.sos.studentonstudy.ui.SosBackground
import com.sos.studentonstudy.ui.SosCard
import com.sos.studentonstudy.ui.SosField
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosStroke
import com.sos.studentonstudy.ui.SosTeal
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.TagChip
import com.sos.studentonstudy.ui.VerifiedBadge
import com.sos.studentonstudy.ui.formatDate
import com.sos.studentonstudy.ui.formatIdr
import com.sos.studentonstudy.ui.helper.RatingLine
import com.sos.studentonstudy.ui.rememberAttachmentPicker

/**
 * Request detail from Figma. Your own requests get the teal "Request" header; other people's show the
 * author and a "Post a reply…" bar.
 */
@Composable
fun RequestDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: RequestDetailViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val item = state.item
    val pickFile = rememberAttachmentPicker(viewModel::onAttachmentChange)

    // The request was deleted (from the edit form), so there is nothing left to show.
    LaunchedEffect(state.loading, item == null) { if (!state.loading && item == null) onBack() }

    Column(modifier = Modifier.fillMaxSize().imePadding()) {
        if (state.isOwn && item != null) {
            OwnRequestHeader(onBack = onBack, onEdit = if (item.request.helperId == null) ({ onEdit(item.request.id) }) else null)
        } else {
            BackTopBar(title = "", onBack = onBack)
        }
        if (item == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (state.loading) CircularProgressIndicator(color = SosPurple)
            }
            return@Column
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { RequestDetailCard(item, showAuthor = !state.isOwn) }
            item { Text("Replies (${state.replies.size})", color = SosText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold) }
            if (state.replies.isEmpty()) {
                item { Text("No replies yet.", color = SosText.copy(alpha = .6f), fontSize = 14.sp) }
            }
            items(state.replies, key = { it.id }) { ReplyCard(it) }
        }
        if (!state.isOwn) {
            Column(modifier = Modifier.fillMaxWidth().background(SosBackground)) {
                state.replyAttachment?.let {
                    AttachmentChip(it, modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp), onRemove = { viewModel.onAttachmentChange(null) })
                }
                MessageField(
                    value = state.replyText,
                    onValueChange = viewModel::onReplyChange,
                    placeholder = "Post a reply…",
                    onAttach = pickFile,
                    onSend = viewModel::postReply,
                    showSend = state.canSend,
                    leading = { Box(Modifier.size(32.dp).background(SosAvatarGray, CircleShape)) }
                )
            }
        }
    }
}

/** Teal header with a centered "Request" title; [onEdit] is null when the request can't be edited. */
@Composable
private fun OwnRequestHeader(onBack: () -> Unit, onEdit: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().background(SosTeal).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = SosPurple)
        }
        Text("Request", color = SosText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        if (onEdit != null) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit request", tint = SosText)
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RequestDetailCard(item: RequestWithMeta, showAuthor: Boolean) {
    val request = item.request
    SosCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (showAuthor) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PersonAvatar(32.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(item.authorName, color = SosText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
            }
            Text(request.title, color = SosText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                request.tagList().firstOrNull()?.let { MetaItem(Icons.Outlined.Category, it) }
                MetaItem(Icons.Outlined.Schedule, EstimatedTime.label(request.estimatedTime))
                MetaItem(Icons.Outlined.Savings, formatIdr(request.budget))
                MetaItem(Icons.Outlined.Event, "Due ${formatDate(request.deadline)}")
            }
            Spacer(Modifier.height(12.dp))
            Text(request.details, color = SosText, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            ImagePlaceholder()
            request.attachmentName?.let {
                Spacer(Modifier.height(12.dp))
                AttachmentChip(it)
            }
            if (request.tagList().isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = SosText.copy(alpha = .6f))
                Spacer(Modifier.height(10.dp))
                Text("Tags", color = SosText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    request.tagList().forEach { TagChip(it) }
                }
            }
        }
    }
}

/** The gray picture box Figma shows under the description. */
@Composable
private fun ImagePlaceholder() {
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Outlined.Image, contentDescription = null, tint = Color(0xFF1F1F1F), modifier = Modifier.size(150.dp))
    }
}

@Composable
private fun ReplyCard(reply: ReplyWithAuthor) {
    SosCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (reply.isHelper) PersonAvatar(32.dp) else InitialAvatar(reply.authorName, 32.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(reply.authorName, color = SosText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        if (reply.isHelper) {
                            Spacer(Modifier.width(6.dp))
                            VerifiedBadge(size = 14.dp)
                        }
                    }
                    if (reply.rating != null && reply.helps != null) RatingLine(reply.rating, reply.helps)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(reply.message, color = SosText, fontSize = 14.sp)
            reply.link?.let {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SosField, RoundedCornerShape(4.dp))
                        .border(1.dp, SosStroke, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Link, contentDescription = null, tint = SosText)
                    Spacer(Modifier.width(12.dp))
                    Text(it, color = SosText, fontSize = 14.sp)
                }
            }
        }
    }
}
