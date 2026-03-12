package com.dhruv.instachatviewer.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipUtils {

    private const val TAG = "ZipUtils"

    /**
     * Extract only message JSON files from the archive into a temporary folder.
     * This reduces retained sensitive data and avoids extracting unrelated files.
     */
    fun unzipToCache(
        context: Context,
        zipUri: Uri,
        targetFolderName: String = "unzipped_${System.currentTimeMillis()}"
    ): File? {
        val cacheRoot = File(context.cacheDir, targetFolderName)
        if (!cacheRoot.exists()) cacheRoot.mkdirs()

        val resolver = context.contentResolver
        var zis: ZipInputStream? = null
        var extractedFiles = 0
        try {
            val inputStream: InputStream = resolver.openInputStream(zipUri) ?: return null
            zis = ZipInputStream(inputStream)
            var ze: ZipEntry? = zis.nextEntry
            while (ze != null) {
                val fileName = ze.name
                val normalizedName = fileName.replace('\\', '/')
                if (!shouldExtract(normalizedName, ze)) {
                    zis.closeEntry()
                    ze = zis.nextEntry
                    continue
                }

                val outFile = File(cacheRoot, fileName).canonicalFile
                if (!outFile.path.startsWith(cacheRoot.canonicalPath + File.separator)) {
                    Log.w(TAG, "Skipped suspicious zip entry")
                    zis.closeEntry()
                    ze = zis.nextEntry
                    continue
                }

                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { fos ->
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val len = zis.read(buffer)
                        if (len <= 0) break
                        fos.write(buffer, 0, len)
                    }
                }

                extractedFiles += 1
                zis.closeEntry()
                ze = zis.nextEntry
            }

            return if (extractedFiles > 0) cacheRoot else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract chat archive", e)
            cacheRoot.deleteRecursively()
            return null
        } finally {
            try { zis?.close() } catch (_: Exception) {}
        }
    }

    private fun shouldExtract(entryName: String, entry: ZipEntry): Boolean {
        if (entry.isDirectory) return false

        val lowerName = entryName.lowercase()
        val isInboxJson = (lowerName.startsWith("messages/inbox/") ||
            lowerName.contains("/messages/inbox/")) &&
            lowerName.endsWith(".json")

        return isInboxJson
    }
}
