package com.sos.studentonstudy

import android.app.Application
import android.content.Context
import com.sos.studentonstudy.data.AppSettings
import com.sos.studentonstudy.data.RequestImageFiles
import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.seedDatabase
import com.sos.studentonstudy.data.local.SosDatabase
import com.sos.studentonstudy.data.remote.SupabaseProvider
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.data.repository.ChatRepository
import com.sos.studentonstudy.data.repository.HelperRepository
import com.sos.studentonstudy.data.repository.ReplyRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
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
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = SosDatabase.create(context)
    private val session = SessionManager(context)
    /** Null when local.properties has no Supabase keys: the app then runs offline only. */
    private val supabase = SupabaseProvider.createOrNull()
    val settings = AppSettings(context)
    val authRepository = AuthRepository(database.userDao(), session, supabase, appScope)
    val requestRepository = RequestRepository(
        database.requestDao(), database.userDao(), session, RequestImageFiles(context, database.requestDao()), supabase
    )
    val helperRepository = HelperRepository(database.helperDao())
    val replyRepository = ReplyRepository(database.replyDao(), session)
    val chatRepository = ChatRepository(database.chatDao(), database.requestDao(), session)

    init {
        appScope.launch {
            seedDatabase(database, online = supabase != null)
            requestRepository.sweepImages()
            authRepository.syncSession()
        }
        if (supabase != null) {
            // Pull the user's data from Supabase whenever someone logs in (and once at start when already logged in).
            appScope.launch {
                session.userId.filterNotNull().distinctUntilChanged().collect { requestRepository.refresh() }
            }
        }
    }
}
