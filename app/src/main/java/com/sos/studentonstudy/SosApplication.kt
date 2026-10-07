package com.sos.studentonstudy

import android.app.Application
import android.content.Context
import com.sos.studentonstudy.data.SessionManager
import com.sos.studentonstudy.data.local.EstimatedTime
import com.sos.studentonstudy.data.local.RequestEntity
import com.sos.studentonstudy.data.local.SosDatabase
import com.sos.studentonstudy.data.local.UserEntity
import com.sos.studentonstudy.data.repository.AuthRepository
import com.sos.studentonstudy.data.repository.RequestRepository
import com.sos.studentonstudy.data.repository.hashPassword
import com.sos.studentonstudy.ui.daysFromToday
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

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { seedDemoAccount() }
    }

    /** Creates demo@sos.com / password123 with a few requests on first launch. */
    private suspend fun seedDemoAccount() {
        val userDao = database.userDao()
        if (userDao.count() > 0) return
        val userId = userDao.insert(
            UserEntity(
                name = "Patrick Jane",
                email = "demo@sos.com",
                passwordHash = hashPassword("password123"),
                major = "Product Design",
                university = "Universitas Pelita Harapan"
            )
        )
        val requestDao = database.requestDao()
        val now = System.currentTimeMillis()
        listOf(
            RequestEntity(
                userId = userId,
                title = "Need help with mobile UI design",
                details = "I want to create a clean and modern homepage layout but don't know how to structure it properly.",
                tags = "Design,UI/UX",
                estimatedTime = EstimatedTime.FAST,
                deadline = daysFromToday(0),
                budget = 15_000,
                createdAt = now - 3 * HOUR
            ),
            RequestEntity(
                userId = userId,
                title = "Make an illustration for a project",
                details = "Looking for a simple flat illustration for my presentation cover.",
                tags = "Design,Illustration",
                estimatedTime = EstimatedTime.EXTENDED,
                deadline = daysFromToday(7),
                budget = 50_000,
                createdAt = now - 2 * HOUR
            ),
            RequestEntity(
                userId = userId,
                title = "Review my Room database schema",
                details = "Please check whether my entities and relations make sense for a CRUD app.",
                tags = "Programming",
                estimatedTime = EstimatedTime.MODERATE,
                deadline = daysFromToday(3),
                budget = 25_000,
                createdAt = now - HOUR
            )
        ).forEach { requestDao.insert(it) }
    }

    private companion object {
        const val HOUR = 60 * 60 * 1000L
    }
}
