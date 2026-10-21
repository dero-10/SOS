package com.sos.studentonstudy.data.repository

import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.MyReply
import com.sos.studentonstudy.data.local.ReplyDao
import com.sos.studentonstudy.data.local.ReplyEntity
import com.sos.studentonstudy.data.local.ReplyWithAuthor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Replies on requests; new replies are written as the logged-in user. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReplyRepository(private val replyDao: ReplyDao, private val session: SessionManager) {
    val myReplies: Flow<List<MyReply>> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else replyDao.observeByUser(id)
    }

    fun replies(requestId: Long): Flow<List<ReplyWithAuthor>> = replyDao.observeForRequest(requestId)

    suspend fun post(requestId: Long, message: String, link: String?) {
        val userId = session.userId.value ?: return
        replyDao.insert(ReplyEntity(requestId = requestId, userId = userId, message = message.trim(), link = link))
    }
}
