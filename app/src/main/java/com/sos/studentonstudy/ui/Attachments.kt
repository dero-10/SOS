package com.sos.studentonstudy.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** A file chosen in the system picker. */
internal class PickedFile(val name: String, val uri: Uri, val mimeType: String?)

/** Returns a function that opens the system file picker and reports the chosen file. */
@Composable
internal fun rememberAttachmentPicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onPicked(PickedFile(context.displayName(it), it, context.contentResolver.getType(it))) }
    }
    return { launcher.launch("*/*") }
}

internal fun Context.displayName(uri: Uri): String =
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    } ?: uri.lastPathSegment ?: "attachment"
