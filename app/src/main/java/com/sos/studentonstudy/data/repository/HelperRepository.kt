package com.sos.studentonstudy.data.repository

import com.sos.studentonstudy.data.local.HelpHistoryEntity
import com.sos.studentonstudy.data.local.HelperDao
import com.sos.studentonstudy.data.local.HelperEntity
import kotlinx.coroutines.flow.Flow

/** Helpers shown in Explore → Helper and on helper profiles. */
interface HelperRepository {
    val helpers: Flow<List<HelperEntity>>
    fun helper(id: Long): Flow<HelperEntity?>
    fun recentHelps(helperId: Long): Flow<List<HelpHistoryEntity>>
}

class HelperRepositoryImpl(private val helperDao: HelperDao) : HelperRepository {
    override val helpers: Flow<List<HelperEntity>> = helperDao.observeAll()

    override fun helper(id: Long): Flow<HelperEntity?> = helperDao.observe(id)

    override fun recentHelps(helperId: Long): Flow<List<HelpHistoryEntity>> = helperDao.observeHistory(helperId)
}
