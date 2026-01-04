package com.dhruv.instachatviewer.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhruv.instachatviewer.data.model.ChatEntity
import com.dhruv.instachatviewer.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val onChatClick: (ChatEntity) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val items = mutableListOf<ChatEntity>()

    fun submitList(list: List<ChatEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(
        private val binding: ItemChatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatEntity) {
            binding.tvChatName.text = chat.chatName

            val lastMsg = chat.lastMessage
            binding.tvLastMessage.text =
                if (lastMsg.isNullOrBlank()) "No messages yet" else lastMsg

            binding.tvTime.text = formatTimestamp(chat.lastTimestamp)

            // first letter avatar
            val initial = chat.chatName.trim().firstOrNull()?.uppercaseChar() ?: '?'
            binding.tvAvatarInitial.text = initial.toString()

            binding.root.setOnClickListener {
                onChatClick(chat)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChatBinding.inflate(inflater, parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    private fun formatTimestamp(ts: Long?): String {
        if (ts == null || ts == 0L) return ""
        return try {
            val date = Date(ts)
            val fmt = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            fmt.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}
