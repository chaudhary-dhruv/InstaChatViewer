package com.dhruv.instachatviewer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val chatId: Long = 0L,
    val chatName: String,
    val isGroup: Boolean = false,
    val lastMessage: String? = null,
    val lastTimestamp: Long? = null,
    val profilePhoto: String? = null
)
