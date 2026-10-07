package com.sos.studentonstudy.ui.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sos.studentonstudy.data.local.EstimatedTime
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.tagList
import com.sos.studentonstudy.ui.SosCard
import com.sos.studentonstudy.ui.SosError
import com.sos.studentonstudy.ui.SosMuted
import com.sos.studentonstudy.ui.SosText
import com.sos.studentonstudy.ui.TagChip
import com.sos.studentonstudy.ui.formatDate
import com.sos.studentonstudy.ui.formatIdr
import com.sos.studentonstudy.ui.isUrgent

/** Request card from the Figma Explore → Request tab. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RequestCard(request: RequestEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    SosCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    request.title,
                    color = SosText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isUrgent(request.deadline)) {
                    Spacer(Modifier.width(8.dp))
                    UrgentBadge()
                }
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                MetaItem(Icons.Outlined.Savings, formatIdr(request.budget))
                MetaItem(Icons.Outlined.Schedule, EstimatedTime.label(request.estimatedTime))
                MetaItem(Icons.Outlined.Event, formatDate(request.deadline))
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                request.tagList().forEach { TagChip(it) }
            }
        }
    }
}

@Composable
internal fun UrgentBadge() {
    Text(
        "Urgent",
        color = Color.White,
        fontSize = 11.sp,
        modifier = Modifier.background(SosError, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
internal fun MetaItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SosMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = SosMuted, fontSize = 12.sp)
    }
}
