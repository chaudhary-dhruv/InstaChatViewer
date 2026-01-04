package com.dhruv.instachatviewer.ui.adapter

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dhruv.instachatviewer.data.model.MessageEntity
import com.dhruv.instachatviewer.databinding.ItemMessageReceivedBinding
import com.dhruv.instachatviewer.databinding.ItemMessageSentBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val chatName: String,        // other person / group name
    private var ownerName: String?       // current user name, can be null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<MessageEntity>()

    companion object {
        private const val TYPE_SENT = 1
        private const val TYPE_RECEIVED = 2

        private val GENERIC_ATTACHMENT_TEXTS = setOf(
            "You sent an attachment.",
            "You sent an attachment"
        )
    }

    fun submitList(list: List<MessageEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun updateOwnerName(name: String) {
        ownerName = name
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val msg = items[position]
        return if (ownerName != null) {
            if (msg.sender == ownerName) TYPE_SENT else TYPE_RECEIVED
        } else {
            // fallback: 1-1 chat ke liye approx
            if (msg.sender == chatName) TYPE_RECEIVED else TYPE_SENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(inflater, parent, false)
                SentViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageReceivedBinding.inflate(inflater, parent, false)
                ReceivedViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = items[position]
        val prev = if (position > 0) items[position - 1] else null
        when (holder) {
            is SentViewHolder -> holder.bind(msg, position, prev)
            is ReceivedViewHolder -> holder.bind(msg, position, prev)
        }
    }

    inner class SentViewHolder(
        private val binding: ItemMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(msg: MessageEntity, position: Int, prev: MessageEntity?) {
            bindDateHeader(binding.tvDateHeader, msg, position, prev)

            val text = buildMessageText(msg)
            binding.tvMessage.text = text

            // 🔗 Make links clickable
            binding.tvMessage.movementMethod = LinkMovementMethod.getInstance()
            binding.tvMessage.isClickable = true
            binding.tvMessage.isFocusable = true

            binding.tvTime.text = formatTime(msg.timestamp)
            binding.tvReaction.text = msg.reaction ?: ""
        }
    }

    inner class ReceivedViewHolder(
        private val binding: ItemMessageReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(msg: MessageEntity, position: Int, prev: MessageEntity?) {
            bindDateHeader(binding.tvDateHeader, msg, position, prev)

            val text = buildMessageText(msg)
            binding.tvMessage.text = text

            // 🔗 Make links clickable
            binding.tvMessage.movementMethod = LinkMovementMethod.getInstance()
            binding.tvMessage.isClickable = true
            binding.tvMessage.isFocusable = true

            binding.tvSender.text = msg.sender
            binding.tvTime.text = formatTime(msg.timestamp)
            binding.tvReaction.text = msg.reaction ?: ""
        }
    }

    private fun buildMessageText(msg: MessageEntity): String {
        val content = fixNull(msg.content)
        val isGenericAttachment = GENERIC_ATTACHMENT_TEXTS.contains(content)
        val hasLink = !msg.shareUrl.isNullOrBlank()

        return when (msg.messageType) {
            "link" -> {
                val link = msg.shareUrl.orEmpty()
                when {
                    isGenericAttachment && link.isNotBlank() -> link
                    content.isBlank() -> link
                    link.isBlank() -> content
                    else -> content + "\n" + link
                }
            }
            "image" -> {
                if (content.isBlank() || isGenericAttachment) "[Image]" else content
            }
            "video" -> {
                if (content.isBlank() || isGenericAttachment) "[Video]" else content
            }
            else -> {
                content
            }
        }
    }

    private fun bindDateHeader(headerView: View, msg: MessageEntity, position: Int, prev: MessageEntity?) {
        val tv = headerView as? android.widget.TextView ?: return
        if (position == 0 || !isSameDay(msg.timestamp, prev?.timestamp ?: 0L)) {
            tv.visibility = View.VISIBLE
            tv.text = formatDay(msg.timestamp)
        } else {
            tv.visibility = View.GONE
        }
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        if (t1 == 0L || t2 == 0L) return false
        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c1.timeInMillis = t1
        c2.timeInMillis = t2
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun formatDay(ts: Long): String {
        if (ts == 0L) return ""
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(ts))
    }

    private fun formatTime(ts: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(ts))
    }

    private fun fixNull(s: String?): String = s ?: ""
}
