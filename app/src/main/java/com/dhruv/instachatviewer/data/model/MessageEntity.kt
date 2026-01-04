package com.dhruv.instachatviewer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "messages",
    indices = [Index(value = ["chatId", "timestamp"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chatId: Long,                // foreign link (we'll set after chat insert)
    val sender: String?,
    val messageType: String,         // "text", "image", "video", "link", "system"
    val content: String?,            // text content or caption
    val timestamp: Long,             // epoch ms
    val reaction: String? = null,    // e.g. "❤️"
    val mediaPath: String? = null,   // local path to extracted media file (if any)
    val shareUrl: String? = null     // reel/post link (if any)
)
