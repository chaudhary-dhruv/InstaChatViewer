package com.dhruv.instachatviewer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dhruv.instachatviewer.data.model.ChatEntity
import com.dhruv.instachatviewer.data.model.MessageEntity

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM chats ORDER BY lastTimestamp DESC")
    suspend fun getAllChats(): List<ChatEntity>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChat(chatId: Long): List<MessageEntity>

    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    @Query("DELETE FROM chats")
    suspend fun clearChats()
}
