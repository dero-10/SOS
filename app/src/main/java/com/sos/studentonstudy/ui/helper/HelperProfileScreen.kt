package com.sos.studentonstudy.ui.helper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sos.studentonstudy.data.local.HelpHistoryEntity
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.local.skillList
import com.sos.studentonstudy.data.local.tagList
import com.sos.studentonstudy.ui.BackTopBar
import com.sos.studentonstudy.ui.EmptyState
import com.sos.studentonstudy.ui.LinkedInBadge
import com.sos.studentonstudy.ui.PersonAvatar
import com.sos.studentonstudy.ui.SectionTitle
import com.sos.studentonstudy.ui.SosButton
import com.sos.studentonstudy.ui.SosCard
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosPurple
import com.sos.studentonstudy.ui.SosStar
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.SosViewModelFactory
import com.sos.studentonstudy.ui.StatBox
import com.sos.studentonstudy.ui.TagChip
import com.sos.studentonstudy.ui.formatAgo

/** Helper profile from Figma: avatar, stats, recent helps, skills and the "Chat Helper" button. */
@Composable
fun HelperProfileScreen(
    onBack: () -> Unit,
    onChatHelper: (Long) -> Unit,
    viewModel: HelperProfileViewModel = viewModel(factory = SosViewModelFactory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val helper = state.helper

    Column(modifier = Modifier.fillMaxSize()) {
        BackTopBar(title = "", onBack = onBack)
        when {
            state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SosPurple)
            }
            helper == null -> EmptyState("Helper not found", "This helper is no longer available.", Modifier.weight(1f))
            else -> {
                HelperProfileContent(helper, state.recentHelps, modifier = Modifier.weight(1f))
                SosButton("Chat Helper", onClick = { onChatHelper(helper.id) }, modifier = Modifier.padding(horizontal = 40.dp, vertical = 20.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HelperProfileContent(helper: HelperEntity, recentHelps: List<HelpHistoryEntity>, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            PersonAvatar(126.dp)
            Spacer(Modifier.height(12.dp))
            Text(helper.name, color = SosText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = SosStar, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("%.1f".format(helper.rating), color = SosMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.School, contentDescription = null, tint = SosText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(helper.education, color = SosText, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
            LinkedInBadge(
                size = 22.dp,
                modifier = Modifier.clickable {
                    // Fails only when no browser is installed; there is nothing useful to show then.
                    runCatching { uriHandler.openUri(helper.linkedinUrl) }
                }
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox(helper.helps.toString(), "Helps", Modifier.weight(1f))
            StatBox(helper.requests.toString(), "Requests", Modifier.weight(1f))
            StatBox(helper.reviews.toString(), "Reviews", Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Recent Helps")
        Spacer(Modifier.height(12.dp))
        if (recentHelps.isEmpty()) {
            Text("No helps yet", color = SosMuted, fontSize = 13.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            recentHelps.forEach { RecentHelpCard(it) }
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Skills")
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            helper.skillList().forEach { TagChip(it) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentHelpCard(help: HelpHistoryEntity) {
    SosCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(help.title, color = SosText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRow(help.stars)
                Spacer(Modifier.width(8.dp))
                Text("•  ${formatAgo(help.completedAt)}", color = SosMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                help.tagList().forEach { TagChip(it) }
            }
        }
    }
}
