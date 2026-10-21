package com.sos.studentonstudy.ui.recent

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.ui.EmptyState
import com.sos.studentonstudy.ui.SectionTitle
import com.sos.studentonstudy.ui.SosCard
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosTeal
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.formatTimeAgo

/** Recent tab: the user's requests and replies, grouped by when they happened. */
@Composable
fun RecentScreen(
    onOpenRequest: (Long) -> Unit,
    viewModel: RecentViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Recent", color = SosText, fontSize = 26.sp, fontWeight = FontWeight.Bold) }
        when {
            state.loading -> item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SosPurple)
                }
            }
            state.groups.isEmpty() -> item { EmptyState("Nothing here yet", "Requests you post, send or reply to will show up here.") }
            else -> state.groups.forEach { (group, entries) ->
                item(key = "group-${group.name}") {
                    SectionTitle(group.label, modifier = Modifier.padding(top = 8.dp))
                }
                items(entries.size, key = { entries[it].key }) { index ->
                    RecentRow(entries[index], onClick = { onOpenRequest(entries[index].requestId) })
                }
            }
        }
    }
}

@Composable
private fun RecentRow(item: RecentItem, onClick: () -> Unit) {
    SosCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(SosTeal, CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    when (item.kind) {
                        RecentKind.Posted -> Icons.Outlined.PostAdd
                        RecentKind.SentToHelper -> Icons.AutoMirrored.Outlined.Send
                        RecentKind.Replied -> Icons.AutoMirrored.Outlined.Reply
                    },
                    contentDescription = null,
                    tint = SosText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = SosText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(item.subtitle, color = SosMuted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Text(formatTimeAgo(item.timeMillis), color = SosMuted, fontSize = 12.sp)
        }
    }
}
