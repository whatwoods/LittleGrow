package com.littlegrow.app.media

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale

data class PendingPhotoCapture(
    val uri: Uri,
    val path: String,
)

object PhotoStore {
    fun createPendingCapture(
        context: Context,
        prefix: String,
    ): PendingPhotoCapture {
        val file = createManagedFile(context, prefix, "jpg")
        return PendingPhotoCapture(
            uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            ),
            path = file.absolutePath,
        )
    }

    fun importPhoto(
        context: Context,
        source: Uri,
        prefix: String,
    ): String {
        val extension = resolveExtension(context, source)
        val destination = createManagedFile(context, prefix, extension)
        context.contentResolver.openInputStream(source)?.use { input ->
            destination.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("无法读取所选照片。")
        return destination.absolutePath
    }

    fun deletePhoto(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    fun toUri(path: String): Uri = Uri.fromFile(File(path))

    private fun createManagedFile(
        context: Context,
        prefix: String,
        extension: String,
    ): File {
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(context.filesDir, "pictures")
        val directory = File(picturesDir, "attachments").apply { mkdirs() }
        val safeExtension = extension.lowercase(Locale.US).trim('.').ifBlank { "jpg" }
        return File.createTempFile("${prefix}_", ".$safeExtension", directory)
    }

    private fun resolveExtension(
        context: Context,
        source: Uri,
    ): String {
        val mimeType = context.contentResolver.getType(source)
        val fromMime = mimeType?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
        val fromPath = source.lastPathSegment
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
        return fromMime ?: fromPath ?: "jpg"
    }
}
