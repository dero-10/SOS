package com.sos.studentonstudy.testutil

import com.sos.studentonstudy.data.local.HelpHistoryEntity
import com.sos.studentonstudy.data.local.HelperEntity
import com.sos.studentonstudy.data.repository.HelperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory HelperRepository. */
class FakeHelperRepository(
    initial: List<HelperEntity> = emptyList(),
    private val history: Map<Long, List<HelpHistoryEntity>> = emptyMap()
) : HelperRepository {
    private val flow = MutableStateFlow(initial)
    override val helpers: Flow<List<HelperEntity>> = flow

    override fun helper(id: Long): Flow<HelperEntity?> = flow.map { list -> list.firstOrNull { it.id == id } }

    override fun recentHelps(helperId: Long): Flow<List<HelpHistoryEntity>> =
        flow.map { history[helperId].orEmpty() }
}
