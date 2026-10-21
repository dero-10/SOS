package com.sos.studentonstudy.data.repository

import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.ChatDao
import com.sos.studentonstudy.data.local.ChatMessageEntity
import com.sos.studentonstudy.data.local.ChatPreview
import com.sos.studentonstudy.data.local.RequestDao
import com.sos.studentonstudy.data.local.RequestEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Conversations between the logged-in user and helpers. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepository(
    private val chatDao: ChatDao,
    private val requestDao: RequestDao,
    private val session: SessionManager
) {
    val previews: Flow<List<ChatPreview>> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else chatDao.observePreviews(id)
    }

    fun messages(helperId: Long): Flow<List<ChatMessageEntity>> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else chatDao.observeMessages(id, helperId)
    }

    suspend fun send(helperId: Long, text: String, attachmentName: String? = null) {
        val userId = session.userId.value ?: return
        chatDao.insert(
            ChatMessageEntity(userId = userId, helperId = helperId, text = text.trim(), attachmentName = attachmentName, fromMe = true)
        )
    }

    /**
     * Sends a request privately to one helper: it becomes the first bubble in their chat and is also
     * recorded as a (non-public) request so it shows up in Recent and on the dashboard.
     */
    suspend fun sendRequest(helperId: Long, request: RequestEntity) {
        val userId = session.userId.value ?: return
        requestDao.insert(request.copy(userId = userId, helperId = helperId))
        chatDao.insert(
            ChatMessageEntity(
                userId = userId,
                helperId = helperId,
                text = request.details,
                attachmentName = request.attachmentName,
                fromMe = true,
                isRequest = true
            )
        )
    }
}
