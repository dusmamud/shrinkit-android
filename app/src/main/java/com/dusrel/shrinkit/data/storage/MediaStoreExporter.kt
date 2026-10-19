package com.dusrel.shrinkit.data.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object MediaStoreExporter {

    private const val DIRECTORY_NAME = "ShrinkIt"

    /**
     * Saves processed image bytes to public storage (Pictures/ShrinkIt) using atomic MediaStore writes.
     */
    fun saveToPictures(
        context: Context,
        data: ByteArray,
        filename: String,
        mimeType: String
    ): Uri {
        val resolver = context.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$DIRECTORY_NAME")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("Failed to insert MediaStore record for $filename")

            try {
                resolver.openOutputStream(uri)?.use { out ->
                    out.write(data)
                    out.flush()
                }

                // Mark atomic write as complete
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                return uri
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        } else {
            // Legacy Storage (API 24-28)
            @Suppress("DEPRECATION")
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val shrinkItDir = File(picturesDir, DIRECTORY_NAME)
            if (!shrinkItDir.exists()) {
                shrinkItDir.mkdirs()
            }

            val targetFile = File(shrinkItDir, filename)
            FileOutputStream(targetFile).use { out ->
                out.write(data)
                out.flush()
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                @Suppress("DEPRECATION")
                put(MediaStore.Images.Media.DATA, targetFile.absolutePath)
            }
            return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: Uri.fromFile(targetFile)
        }
    }

    /**
     * Saves a temporary file to app-internal cache for fast previewing and sharing.
     * Prunes cached files older than 7 days on each save to avoid unbounded growth.
     */
    fun saveToInternalCache(
        context: Context,
        data: ByteArray,
        filename: String
    ): File {
        val cacheDir = File(context.cacheDir, "processed")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        pruneOldCache(cacheDir, maxAgeMs = 7L * 24 * 60 * 60 * 1000)
        val file = File(cacheDir, filename)
        FileOutputStream(file).use { out ->
            out.write(data)
            out.flush()
        }
        return file
    }

    private fun pruneOldCache(cacheDir: File, maxAgeMs: Long) {
        try {
            val cutoff = System.currentTimeMillis() - maxAgeMs
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoff) {
                    runCatching { file.delete() }
                }
            }
        } catch (_: Exception) {
            // Cache pruning is best-effort; never fail the save.
        }
    }
}
