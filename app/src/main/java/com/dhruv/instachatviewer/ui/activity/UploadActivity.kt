package com.dhruv.instachatviewer.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dhruv.instachatviewer.data.model.ChatEntity
import com.dhruv.instachatviewer.data.model.MessageEntity
import com.dhruv.instachatviewer.data.repository.ChatRepository
import com.dhruv.instachatviewer.databinding.ActivityUploadBinding
import com.dhruv.instachatviewer.utils.JsonParser
import com.dhruv.instachatviewer.utils.Prefs
import com.dhruv.instachatviewer.utils.SecurityUtils
import com.dhruv.instachatviewer.utils.ZipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UploadActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UploadActivity"
    }

    private lateinit var binding: ActivityUploadBinding
    private lateinit var repository: ChatRepository
    private var importJob: Job? = null

    private val pickZipLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                grantReadAccess(it)
                binding.tvFileName.text = getFileName(it)
                startProcessingZip(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecurityUtils.protectSensitiveContent(this)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ChatRepository(applicationContext)

        binding.btnSelectFile.setOnClickListener {
            pickZipLauncher.launch(
                arrayOf(
                    "application/zip",
                    "application/x-zip-compressed",
                    "application/octet-stream"
                )
            )
        }

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        importJob?.cancel()
        super.onDestroy()
    }

    private fun getFileName(uri: Uri): String {
        var name = "instagram_export.zip"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun startProcessingZip(uri: Uri) {
        if (!isZipFile(uri)) {
            binding.tvStatus.text = "Please choose the Instagram export ZIP file."
            binding.progress.visibility = View.GONE
            binding.btnNext.isEnabled = false
            return
        }

        importJob?.cancel()
        binding.progress.visibility = View.VISIBLE
        binding.tvStatus.text = "Extracting archive..."
        binding.btnNext.isEnabled = false

        importJob = lifecycleScope.launch(Dispatchers.IO) {
            var unzippedRoot: File? = null
            try {
                unzippedRoot = ZipUtils.unzipToCache(this@UploadActivity, uri)
                if (unzippedRoot == null) {
                    withContext(Dispatchers.Main) {
                        binding.tvStatus.text = "Could not read the Instagram export ZIP."
                        binding.progress.visibility = View.GONE
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "Locating inbox folder..."
                }

                val inboxFolder = findInboxFolder(unzippedRoot)
                if (inboxFolder == null || !inboxFolder.exists()) {
                    withContext(Dispatchers.Main) {
                        binding.tvStatus.text = "No chats found. Export messages as JSON and try again."
                        binding.progress.visibility = View.GONE
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "Parsing chats..."
                }

                val parsed: Map<String, List<MessageEntity>> = JsonParser.parseInboxFolder(inboxFolder)
                if (parsed.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.tvStatus.text = "No readable chats were found in this archive."
                        binding.progress.visibility = View.GONE
                    }
                    return@launch
                }

                repository.clearAll()

                for ((chatName, messages) in parsed) {
                    val lastMsg = messages.lastOrNull()
                    val chatEntity = ChatEntity(
                        chatName = chatName,
                        isGroup = false,
                        lastMessage = lastMsg?.content ?: "",
                        lastTimestamp = lastMsg?.timestamp,
                        profilePhoto = null
                    )
                    repository.insertChatWithMessages(chatEntity, messages)
                }

                Prefs.setImported(this@UploadActivity, true)

                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "Import complete. ${parsed.size} chats are ready to explore."
                    binding.progress.visibility = View.GONE
                    binding.btnNext.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process Instagram export", e)
                withContext(Dispatchers.Main) {
                    binding.tvStatus.text =
                        e.localizedMessage ?: "Something went wrong while importing your chats."
                    binding.progress.visibility = View.GONE
                    binding.btnNext.isEnabled = false
                }
            } finally {
                unzippedRoot?.deleteRecursively()
            }
        }
    }

    private fun findInboxFolder(rootDir: File): File? {
        rootDir.walk().forEach { file ->
            if (file.isDirectory && file.path.replace("\\", "/").endsWith("messages/inbox")) {
                return file
            }
        }
        return null
    }

    private fun isZipFile(uri: Uri): Boolean {
        val fileName = getFileName(uri).lowercase()
        val mimeType = contentResolver.getType(uri)?.lowercase().orEmpty()
        return fileName.endsWith(".zip") ||
            mimeType.contains("zip") ||
            mimeType == "application/octet-stream"
    }

    private fun grantReadAccess(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        } catch (_: UnsupportedOperationException) {
        }
    }
}
