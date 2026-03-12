package com.dhruv.instachatviewer.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhruv.instachatviewer.data.model.ChatEntity
import com.dhruv.instachatviewer.data.repository.ChatRepository
import com.dhruv.instachatviewer.databinding.ActivityChatListBinding
import com.dhruv.instachatviewer.ui.adapter.ChatListAdapter
import com.dhruv.instachatviewer.utils.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private lateinit var repository: ChatRepository
    private lateinit var adapter: ChatListAdapter

    private var allChats: List<ChatEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SecurityUtils.protectSensitiveContent(this)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ChatRepository(applicationContext)

        setupRecyclerView()
        setupActions()
        setupSearch()
        loadChats()
    }

    override fun onResume() {
        super.onResume()
        loadChats()
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(::openChat)
        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = adapter
    }

    private fun setupActions() {
        binding.btnImportMore.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }

        binding.btnEmptyImport.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChats(s?.toString().orEmpty())
            }
        })
    }

    private fun loadChats() {
        binding.progress.visibility = View.VISIBLE
        binding.emptyStateGroup.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val chats = repository.getAllChats()
            allChats = chats
            withContext(Dispatchers.Main) {
                binding.progress.visibility = View.GONE
                updateSummary(chats)

                if (chats.isEmpty()) {
                    adapter.submitList(emptyList())
                    binding.rvChats.visibility = View.GONE
                    binding.emptyStateGroup.visibility = View.VISIBLE
                } else {
                    binding.rvChats.visibility = View.VISIBLE
                    binding.emptyStateGroup.visibility = View.GONE
                    filterChats(binding.etSearch.text?.toString().orEmpty())
                }
            }
        }
    }

    private fun filterChats(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isBlank()) {
            allChats
        } else {
            allChats.filter { it.chatName.lowercase().contains(q) }
        }

        adapter.submitList(filtered)
        binding.tvSearchMeta.text = if (allChats.isEmpty()) {
            "Import a ZIP to start browsing your conversations."
        } else {
            "${filtered.size} of ${allChats.size} chats shown"
        }
    }

    private fun updateSummary(chats: List<ChatEntity>) {
        binding.tvChatCount.text = chats.size.toString()
        binding.tvSummary.text = if (chats.isEmpty()) {
            "Your imported Instagram chats will appear here once the ZIP is processed."
        } else {
            "Jump back into old conversations, track timelines, and search by name instantly."
        }
    }

    private fun openChat(chat: ChatEntity) {
        val intent = Intent(this, ChatDetailActivity::class.java)
        intent.putExtra("chat_id", chat.chatId)
        intent.putExtra("chat_name", chat.chatName)
        startActivity(intent)
    }
}
