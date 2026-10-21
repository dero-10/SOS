package com.sos.studentonstudy.ui.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sos.studentonstudy.data.repository.HelperRepository
import com.sos.studentonstudy.data.repository.ReplyRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import com.sos.studentonstudy.ui.formatIdr
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class RecentKind { Posted, SentToHelper, Replied }

/** One line of the user's activity; tapping it opens [requestId]. */
data class RecentItem(
    val key: String,
    val kind: RecentKind,
    val title: String,
    val subtitle: String,
    val timeMillis: Long,
    val requestId: Long
)

enum class RecentGroup(val label: String) { Today("Today"), ThisWeek("Earlier this week"), Earlier("Earlier") }

data class RecentUiState(
    val groups: List<Pair<RecentGroup, List<RecentItem>>> = emptyList(),
    val loading: Boolean = true
)

class RecentViewModel(
    requests: RequestRepository,
    replies: ReplyRepository,
    helpers: HelperRepository
) : ViewModel() {
    val uiState: StateFlow<RecentUiState> = combine(requests.myRequests, replies.myReplies, helpers.helpers) { mine, myReplies, helperList ->
        val helperNames = helperList.associate { it.id to it.name }
        val items = mine.map { item ->
            val request = item.request
            if (request.helperId == null) {
                RecentItem("request-${request.id}", RecentKind.Posted, request.title, "You posted a request · ${formatIdr(request.budget)}", request.createdAt, request.id)
            } else {
                val name = helperNames[request.helperId] ?: "a helper"
                RecentItem("request-${request.id}", RecentKind.SentToHelper, request.title, "Sent to $name · ${formatIdr(request.budget)}", request.createdAt, request.id)
            }
        } + myReplies.map { reply ->
            RecentItem("reply-${reply.replyId}", RecentKind.Replied, reply.requestTitle, "You replied to this request", reply.createdAt, reply.requestId)
        }
        val todayStart = startOfToday()
        val weekStart = todayStart - 6 * DAY_MILLIS
        val sorted = items.sortedByDescending { it.timeMillis }
        RecentUiState(
            groups = RecentGroup.entries.map { group ->
                group to sorted.filter {
                    when (group) {
                        RecentGroup.Today -> it.timeMillis >= todayStart
                        RecentGroup.ThisWeek -> it.timeMillis in weekStart until todayStart
                        RecentGroup.Earlier -> it.timeMillis < weekStart
                    }
                }
            }.filter { it.second.isNotEmpty() },
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecentUiState())

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
