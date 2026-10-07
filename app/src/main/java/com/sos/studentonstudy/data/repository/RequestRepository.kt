package com.sos.studentonstudy.data.repository

import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.RequestDao
import com.sos.studentonstudy.data.local.RequestEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Help requests owned by the logged-in user. */
@OptIn(ExperimentalCoroutinesApi::class)
class RequestRepository(private val requestDao: RequestDao, private val session: SessionManager) {
    val requests: Flow<List<RequestEntity>> = session.userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else requestDao.observeAll(id)
    }

    suspend fun get(id: Long): RequestEntity? = requestDao.getById(id)

    suspend fun save(request: RequestEntity) {
        if (request.id == 0L) {
            val userId = session.userId.value ?: return
            requestDao.insert(request.copy(userId = userId))
        } else {
            requestDao.update(request)
        }
    }

    suspend fun delete(request: RequestEntity) = requestDao.delete(request)
}
