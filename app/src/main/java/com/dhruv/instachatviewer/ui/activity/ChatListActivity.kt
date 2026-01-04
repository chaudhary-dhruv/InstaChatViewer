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
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ChatRepository(applicationContext)

        setupRecyclerView()
        setupSearch()
        loadChats()
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter { chat ->
            openChat(chat)
        }
        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = adapter
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
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            val chats = repository.getAllChats()
            allChats = chats
            withContext(Dispatchers.Main) {
                binding.progress.visibility = View.GONE
                if (chats.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    adapter.submitList(chats)
                }
            }
        }
    }

    private fun filterChats(query: String) {
        if (allChats.isEmpty()) return
        val q = query.lowercase()
        val filtered = if (q.isBlank()) {
            allChats
        } else {
            allChats.filter { it.chatName.lowercase().contains(q) }
        }
        adapter.submitList(filtered)
    }

    private fun openChat(chat: ChatEntity) {
        val intent = Intent(this, ChatDetailActivity::class.java)
        intent.putExtra("chat_id", chat.chatId)
        intent.putExtra("chat_name", chat.chatName)
        startActivity(intent)
    }
}
