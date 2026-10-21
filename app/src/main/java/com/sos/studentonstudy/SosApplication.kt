package com.sos.studentonstudy

import android.app.Application
import android.content.Context
import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.seedDatabase
import com.sos.studentonstudy.data.local.SosDatabase
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.data.repository.ChatRepository
import com.sos.studentonstudy.data.repository.HelperRepository
import com.sos.studentonstudy.data.repository.ReplyRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SosApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Manual dependency container shared by every ViewModel. */
class AppContainer(context: Context) {
    private val database = SosDatabase.create(context)
    private val session = SessionManager(context)
    val authRepository = AuthRepository(database.userDao(), session)
    val requestRepository = RequestRepository(database.requestDao(), session)
    val helperRepository = HelperRepository(database.helperDao())
    val replyRepository = ReplyRepository(database.replyDao(), session)
    val chatRepository = ChatRepository(database.chatDao(), database.requestDao(), session)

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { seedDatabase(database) }
    }
}
