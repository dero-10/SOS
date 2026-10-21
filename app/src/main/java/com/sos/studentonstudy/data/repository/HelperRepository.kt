package com.sos.studentonstudy.data.repository

import com.sos.studentonstudy.data.local.HelpHistoryEntity
import com.sos.studentonstudy.data.local.HelperDao
import com.sos.studentonstudy.data.local.HelperEntity
import kotlinx.coroutines.flow.Flow

/** Helpers shown in Explore → Helper and on helper profiles. */
class HelperRepository(private val helperDao: HelperDao) {
    val helpers: Flow<List<HelperEntity>> = helperDao.observeAll()

    fun helper(id: Long): Flow<HelperEntity?> = helperDao.observe(id)

    fun recentHelps(helperId: Long): Flow<List<HelpHistoryEntity>> = helperDao.observeHistory(helperId)
}
