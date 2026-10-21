package com.sos.studentonstudy.ui.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.local.skillList
import com.sos.studentonstudy.ui.PersonAvatar
import com.sos.studentonstudy.ui.SosCard
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosStar
import com.sos.studentonstudy.ui.SosSuccess
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.TagChip
import com.sos.studentonstudy.ui.VerifiedBadge

/** Helper card from the Figma Explore → Helper tab. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HelperCard(helper: HelperEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SosCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PersonAvatar(32.dp)
                Spacer(Modifier.width(8.dp))
                Text(helper.name, color = SosText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(6.dp))
                VerifiedBadge()
                Spacer(Modifier.weight(1f))
                OnlinePill(helper.online)
            }
            Spacer(Modifier.height(6.dp))
            RatingLine(helper.rating, helper.helps)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.School, contentDescription = null, tint = SosText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(helper.education, color = SosText, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                helper.skillList().forEach { TagChip(it) }
            }
        }
    }
}

/** Green "Online" or gray "Online today" pill. */
@Composable
internal fun OnlinePill(online: Boolean, modifier: Modifier = Modifier) {
    val color = if (online) SosSuccess else SosMuted.copy(alpha = .5f)
    Row(
        modifier = modifier
            .border(1.dp, color, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(if (online) "Online" else "Online today", color = SosText, fontSize = 11.sp)
    }
}

/** "★ 4.9 • 23 helps". */
@Composable
internal fun RatingLine(rating: Double, helps: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = SosStar, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text("%.1f  •  %d helps".format(rating, helps), color = SosMuted, fontSize = 12.sp)
    }
}

/** Five stars, [filled] of them solid. */
@Composable
internal fun StarRow(filled: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        repeat(5) { index ->
            Icon(
                if (index < filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = SosStar,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
