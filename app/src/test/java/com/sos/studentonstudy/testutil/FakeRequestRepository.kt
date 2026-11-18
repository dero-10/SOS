package com.sos.studentonstudy.testutil

import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.RequestWithMeta
import com.sos.studentonstudy.data.repository.RequestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory request source shared by Explore and Dashboard tests. */
class FakeRequestRepository(
    initialFeed: List<RequestWithMeta> = emptyList(),
    initialOwn: List<RequestWithMeta> = emptyList(),
    override val currentUserId: Long? = 1L
) : RequestRepository {
    val feedItems = MutableStateFlow(initialFeed)
    val ownItems = MutableStateFlow(initialOwn)
    var refreshCalls = 0
        private set

    override val feed: Flow<List<RequestWithMeta>> = feedItems
    override val myRequests: Flow<List<RequestWithMeta>> = ownItems

    override fun observe(id: Long): Flow<RequestWithMeta?> =
        ownItems.map { items -> items.firstOrNull { it.request.id == id } }

    override suspend fun get(id: Long): RequestEntity? =
        ownItems.value.firstOrNull { it.request.id == id }?.request

    override suspend fun refresh(): Boolean {
        refreshCalls++
        return true
    }

    override suspend fun save(request: RequestEntity): Boolean {
        val updated = RequestWithMeta(request, "Student", 0)
        ownItems.value = ownItems.value.filterNot { it.request.id == request.id } + updated
        return true
    }

    override suspend fun delete(request: RequestEntity): Boolean {
        ownItems.value = ownItems.value.filterNot { it.request.id == request.id }
        feedItems.value = feedItems.value.filterNot { it.request.id == request.id }
        return true
    }

    override suspend fun sweepImages() = Unit
}
