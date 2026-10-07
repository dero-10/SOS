package com.sos.studentonstudy.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.ui.EmptyState
import com.sos.studentonstudy.ui.InitialAvatar
import com.sos.studentonstudy.ui.SectionTitle
import com.sos.studentonstudy.ui.SosButton
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosTeal
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.StatBox
import com.sos.studentonstudy.ui.formatIdr
import com.sos.studentonstudy.ui.request.RequestCard

/** Home tab: live summary of the user's requests from Room. */
@Composable
fun DashboardScreen(
    onAddRequest: () -> Unit,
    onOpenRequest: (Long) -> Unit,
    onSeeAll: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hello,", color = SosMuted, fontSize = 15.sp)
                    Text(state.name, color = SosText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
                InitialAvatar(state.name, 52.dp)
            }
        }
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = SosTeal, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("Your Balance", color = SosText, fontSize = 13.sp)
                    Text(formatIdr(state.balance), color = SosText, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatBox(state.total.toString(), "Requests", Modifier.weight(1f))
                StatBox(state.urgent.toString(), "Urgent", Modifier.weight(1f))
                StatBox(state.dueThisWeek.toString(), "This week", Modifier.weight(1f))
            }
        }
        item { SosButton("Post a Request", onAddRequest) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Recent Requests", modifier = Modifier.weight(1f))
                TextButton(onClick = onSeeAll) { Text("See all", color = SosPurple, fontWeight = FontWeight.SemiBold) }
            }
        }
        when {
            state.loading -> item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SosPurple)
                }
            }
            state.recent.isEmpty() -> item { EmptyState("No requests yet", "Post your first request to get help.") }
            else -> items(state.recent, key = { it.id }) { request ->
                RequestCard(request, onClick = { onOpenRequest(request.id) })
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}
