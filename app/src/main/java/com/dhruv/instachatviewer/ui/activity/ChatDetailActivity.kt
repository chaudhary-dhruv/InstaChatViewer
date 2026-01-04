package com.dhruv.instachatviewer.ui.activity

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhruv.instachatviewer.data.model.MessageEntity
import com.dhruv.instachatviewer.data.repository.ChatRepository
import com.dhruv.instachatviewer.databinding.ActivityChatDetailBinding
import com.dhruv.instachatviewer.ui.adapter.MessageAdapter
import com.dhruv.instachatviewer.utils.Prefs
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private lateinit var repository: ChatRepository
    private lateinit var adapter: MessageAdapter

    private var chatId: Long = -1L
    private var chatName: String = ""

    private var allMessages: List<MessageEntity> = emptyList()
    private var ownerName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ChatRepository(applicationContext)

        chatId = intent.getLongExtra("chat_id", -1L)
        chatName = intent.getStringExtra("chat_name") ?: ""

        if (chatId == -1L) {
            finish()
            return
        }

        ownerName = Prefs.getOwnerName(this)

        setupUi()
        loadMessages()

        if (ownerName == null) {
            askOwnerNameOnce()
        }
    }

    private fun setupUi() {
        binding.tvChatName.text = chatName

        adapter = MessageAdapter(chatName, ownerName)
        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        binding.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.ivDate.setOnClickListener {
            openDatePicker()
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch(Dispatchers.IO) {
            val msgs = repository.getMessagesForChat(chatId)
            allMessages = msgs
            withContext(Dispatchers.Main) {
                adapter.submitList(msgs)
                if (msgs.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(msgs.size - 1)
                }
            }
        }
    }

    private fun openDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            if (selection != null && allMessages.isNotEmpty()) {
                val dayStart = getStartOfDayMillis(selection)
                val index = allMessages.indexOfFirst { it.timestamp >= dayStart }
                if (index != -1) {
                    binding.rvMessages.scrollToPosition(index)
                }
            }
        }

        picker.show(supportFragmentManager, "date_picker")
    }

    private fun getStartOfDayMillis(selectedUtcMillis: Long): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = selectedUtcMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun askOwnerNameOnce() {
        val input = EditText(this)
        input.hint = "Your Instagram name"

        AlertDialog.Builder(this)
            .setTitle("Who's \"you\" in chats?")
            .setMessage("Enter your Instagram name exactly as it appears in messages.\nGroup chats me bubbles sahi dikhenge.")
            .setView(input)
            .setPositiveButton("Save") { dialog, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    Prefs.setOwnerName(this, name)
                    ownerName = name
                    adapter.updateOwnerName(name)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
