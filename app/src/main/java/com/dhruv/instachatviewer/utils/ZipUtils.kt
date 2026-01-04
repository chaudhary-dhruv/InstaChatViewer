package com.dhruv.instachatviewer.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ZipUtils {

    /**
     * Unzip the given content URI into a newly created folder inside context.cacheDir.
     * Returns the folder File if success, or null on failure.
     */
    fun unzipToCache(context: Context, zipUri: Uri, targetFolderName: String = "unzipped_${System.currentTimeMillis()}"): File? {
        val cacheRoot = File(context.cacheDir, targetFolderName)
        if (!cacheRoot.exists()) cacheRoot.mkdirs()

        val resolver = context.contentResolver
        var zis: ZipInputStream? = null
        try {
            val inputStream: InputStream = resolver.openInputStream(zipUri) ?: return null
            zis = ZipInputStream(inputStream)
            var ze: ZipEntry? = zis.nextEntry
            while (ze != null) {
                val fileName = ze.name
                // prevent Zip Slip attacks
                val outFile = File(cacheRoot, fileName).canonicalFile
                if (!outFile.path.startsWith(cacheRoot.canonicalPath + File.separator)) {
                    // suspicious entry; skip
                    ze = zis.nextEntry
                    continue
                }

                if (ze.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        val buffer = ByteArray(8 * 1024)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                zis.closeEntry()
                ze = zis.nextEntry
            }
            return cacheRoot
        } catch (e: Exception) {
            e.printStackTrace()
            // cleanup on failure
            cacheRoot.deleteRecursively()
            return null
        } finally {
            try { zis?.close() } catch (_: Exception) {}
        }
    }
}
