package com.sos.studentonstudy.ui.request

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/** Copies picked request images into app-private storage so they stay readable after the picker's grant ends. */
internal object ImageStore {
    private const val DIRECTORY = "request_images"
    private const val MAX_BYTES = 8L * 1024 * 1024
    private const val MAX_EDGE = 1024

    /** Returns the stored file's path, or null when the file can't be read or is larger than 8 MB. */
    suspend fun import(context: Context, uri: Uri, mimeType: String?): String? = withContext(Dispatchers.IO) {
        val extension = mimeType?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) } ?: "jpg"
        val directory = File(context.filesDir, DIRECTORY).apply { mkdirs() }
        val target = File(directory, "${UUID.randomUUID()}.$extension")
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            input.use { source -> target.outputStream().use { source.copyTo(it) } }
            if (target.length() > MAX_BYTES) {
                target.delete()
                null
            } else {
                target.absolutePath
            }
        } catch (e: IOException) {
            target.delete()
            null
        }
    }

    /** Decodes [path] scaled down so its longest edge is about [MAX_EDGE] px. */
    fun decode(path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}

/** The request's picture, or the gray placeholder from Figma when it has none (or it can't be read). */
@Composable
internal fun RequestImage(path: String?, modifier: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        value = path?.let { withContext(Dispatchers.IO) { ImageStore.decode(it)?.asImageBitmap() } }
    }
    Box(modifier = modifier.background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
        val image = bitmap
        if (image != null) {
            Image(image, contentDescription = "Request image", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = Color(0xFF1F1F1F), modifier = Modifier.size(150.dp))
        }
    }
}
