package com.sos.studentonstudy.testutil

import com.sos.studentonstudy.data.local.ChatMessageEntity
import com.sos.studentonstudy.data.local.ChatPreview
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory ChatRepository: push conversations through [setPreviews]. */
class FakeChatRepository(initial: List<ChatPreview> = emptyList()) : ChatRepository {
    private val previewFlow = MutableStateFlow(initial)
    private val messageFlow = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    override val previews: Flow<List<ChatPreview>> = previewFlow

    fun setPreviews(value: List<ChatPreview>) { previewFlow.value = value }

    override fun messages(helperId: Long): Flow<List<ChatMessageEntity>> =
        messageFlow.map { all -> all.filter { it.helperId == helperId } }

    override suspend fun send(helperId: Long, text: String, attachmentName: String?) {
        messageFlow.value = messageFlow.value +
            ChatMessageEntity(userId = 1, helperId = helperId, text = text.trim(), attachmentName = attachmentName, fromMe = true)
    }

    override suspend fun sendRequest(helperId: Long, request: RequestEntity) = Unit
}
