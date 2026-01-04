package com.dhruv.instachatviewer.data.repository

import android.content.Context
import com.dhruv.instachatviewer.data.db.AppDatabase
import com.dhruv.instachatviewer.data.model.ChatEntity
import com.dhruv.instachatviewer.data.model.MessageEntity

class ChatRepository(context: Context) {
    private val dao = AppDatabase.get(context).chatDao()

    suspend fun insertChatWithMessages(chat: ChatEntity, messages: List<MessageEntity>): Long {
        val chatId = dao.insertChat(chat)
        val messagesWithChatId = messages.map { it.copy(chatId = chatId) }
        dao.insertMessages(messagesWithChatId)
        return chatId
    }

    suspend fun clearAll() {
        dao.clearMessages()
        dao.clearChats()
    }

    // ✅ Chats listing
    suspend fun getAllChats(): List<ChatEntity> = dao.getAllChats()

    // ✅ Messages for a specific chat
    suspend fun getMessagesForChat(chatId: Long): List<MessageEntity> =
        dao.getMessagesForChat(chatId)
}
