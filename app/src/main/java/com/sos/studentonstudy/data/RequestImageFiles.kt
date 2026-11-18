package com.sos.studentonstudy.data

import android.content.Context
import com.sos.studentonstudy.data.local.RequestDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Keeps the app-private request pictures in step with the database so unused files don't pile up. */
class RequestImageFiles(context: Context, private val requestDao: RequestDao) {
    private val directory = File(context.applicationContext.filesDir, DIRECTORY)

    /** Deletes [path] if it lives in the picture folder. */
    suspend fun delete(path: String?) {
        if (path == null) return
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (file.parentFile?.canonicalPath == directory.canonicalPath) file.delete()
        }
    }

    /** Deletes every picture no request refers to, e.g. images picked and then discarded in the form. */
    suspend fun sweepOrphans() = withContext(Dispatchers.IO) {
        val used = requestDao.allImagePaths().map { File(it).canonicalPath }.toSet()
        directory.listFiles()?.forEach { if (it.isFile && it.canonicalPath !in used) it.delete() }
    }

    companion object {
        const val DIRECTORY = "request_images"
    }
}
