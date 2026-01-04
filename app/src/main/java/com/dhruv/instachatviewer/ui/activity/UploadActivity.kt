package com.dhruv.instachatviewer.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dhruv.instachatviewer.data.model.ChatEntity
import com.dhruv.instachatviewer.data.model.MessageEntity
import com.dhruv.instachatviewer.data.repository.ChatRepository
import com.dhruv.instachatviewer.databinding.ActivityUploadBinding
import com.dhruv.instachatviewer.utils.JsonParser
import com.dhruv.instachatviewer.utils.Prefs
import com.dhruv.instachatviewer.utils.ZipUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var selectedUri: Uri? = null
    private lateinit var repository: ChatRepository

    private val pickZipLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedUri = it
                binding.tvFileName.text = getFileName(uri)
                startProcessingZip(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ChatRepository(applicationContext)

        binding.btnSelectFile.setOnClickListener {
            pickZipLauncher.launch("*/*")
        }

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
            finish()
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "unknown.zip"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun startProcessingZip(uri: Uri) {
        binding.progress.visibility = View.VISIBLE
        binding.tvStatus.text = "Extracting archive..."
        binding.btnNext.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val unzippedRoot: File? = ZipUtils.unzipToCache(this@UploadActivity, uri)
                if (unzippedRoot == null) {
                    withContext(Dispatchers.Main) {
                        binding.tvStatus.text = "Failed to extract zip."
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
                        binding.tvStatus.text =
                            "No chats found in archive (messages/inbox missing)."
                        binding.progress.visibility = View.GONE
                    }
                    unzippedRoot.deleteRecursively()
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "Parsing chats..."
                }

                val parsed: Map<String, List<MessageEntity>> =
                    JsonParser.parseInboxFolder(inboxFolder)

                if (parsed.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.tvStatus.text = "No chats found in archive."
                        binding.progress.visibility = View.GONE
                    }
                    unzippedRoot.deleteRecursively()
                    return@launch
                }

                // ❗Clear old data first so duplicates na bane
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

                // mark as imported
                Prefs.setImported(this@UploadActivity, true)

                withContext(Dispatchers.Main) {
                    binding.tvStatus.text =
                        "Parsing complete. Imported ${parsed.size} chats."
                    binding.progress.visibility = View.GONE
                    binding.btnNext.isEnabled = true
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "Error parsing file: ${e.localizedMessage}"
                    binding.progress.visibility = View.GONE
                }
            }
        }
    }

    private fun findInboxFolder(rootDir: File): File? {
        rootDir.walk().forEach { file ->
            if (file.isDirectory &&
                file.path.replace("\\", "/").endsWith("messages/inbox")
            ) {
                return file
            }
        }
        return null
    }
}
