package com.dhruv.instachatviewer.utils

import com.dhruv.instachatviewer.data.model.MessageEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object JsonParser {

    fun parseInboxFolder(inboxFolder: File): Map<String, List<MessageEntity>> {
        val results = mutableMapOf<String, MutableList<MessageEntity>>()

        if (!inboxFolder.exists() || !inboxFolder.isDirectory) return emptyMap()

        val chatFolders = inboxFolder.listFiles()?.filter { it.isDirectory } ?: emptyList()

        for (chatFolder in chatFolders) {
            val messageEntities = mutableListOf<MessageEntity>()
            val jsonFiles = chatFolder.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".json") }
                ?.sortedBy { it.name }
                ?: emptyList()

            var chatTitleFromJson: String? = null

            for (jf in jsonFiles) {
                try {
                    // read as ISO_8859_1 then fix to UTF-8 => common mojibake fix
                    val raw = jf.readText(Charsets.ISO_8859_1)
                    val text = String(raw.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)

                    val jo = JSONObject(text)

                    if (chatTitleFromJson == null && jo.has("title")) {
                        chatTitleFromJson = fixEncoding(jo.optString("title"))
                    }

                    val msgs: JSONArray? =
                        if (jo.has("messages")) jo.getJSONArray("messages") else null

                    if (msgs != null) {
                        for (i in 0 until msgs.length()) {
                            val m = msgs.getJSONObject(i)

                            val sender = fixEncoding(m.optString("sender_name", "Unknown"))
                            val ts = m.optLong("timestamp_ms", 0L)
                            val rawContent = when {
                                m.has("content") -> m.optString("content")
                                m.has("text") -> m.optString("text")
                                else -> null
                            }
                            val content = fixEncoding(rawContent)

                            var reaction: String? = null
                            if (m.has("reactions")) {
                                val ra = m.optJSONArray("reactions")
                                if (ra != null && ra.length() > 0) {
                                    val r0 = ra.optJSONObject(0)
                                    reaction = fixEncoding(r0?.optString("reaction"))
                                }
                            }

                            var shareUrl: String? = null
                            if (m.has("share")) {
                                val s = m.optJSONObject("share")
                                shareUrl = fixEncoding(s?.optString("link"))
                                if (shareUrl.isNullOrBlank()) {
                                    shareUrl = fixEncoding(s?.optString("url"))
                                }
                            }

                            var mediaPath: String? = null
                            if (m.has("photos")) {
                                val photos = m.optJSONArray("photos")
                                if (photos != null && photos.length() > 0) {
                                    val p0 = photos.getJSONObject(0)
                                    mediaPath = fixEncoding(p0.optString("uri", null))
                                }
                            }
                            if (mediaPath == null && m.has("videos")) {
                                val videos = m.optJSONArray("videos")
                                if (videos != null && videos.length() > 0) {
                                    val v0 = videos.getJSONObject(0)
                                    mediaPath = fixEncoding(v0.optString("uri", null))
                                }
                            }

                            val type = when {
                                !shareUrl.isNullOrBlank() -> "link"
                                !mediaPath.isNullOrBlank() ->
                                    if (mediaPath.endsWith(".mp4", true) ||
                                        mediaPath.endsWith(".mov", true)
                                    ) "video" else "image"
                                !content.isNullOrBlank() -> "text"
                                else -> "system"
                            }

                            val msgEntity = MessageEntity(
                                id = 0L,
                                chatId = 0L,
                                sender = sender,
                                messageType = type,
                                content = content,
                                timestamp = ts,
                                reaction = reaction,
                                mediaPath = mediaPath,
                                shareUrl = shareUrl
                            )
                            messageEntities.add(msgEntity)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val chatKey = chatTitleFromJson ?: chatFolder.name
            messageEntities.sortBy { it.timestamp }
            results[chatKey] = messageEntities
        }

        return results
    }

    // Try to convert mojibake "ðŸ˜…" style to proper UTF-8
    private fun fixEncoding(s: String?): String? {
        if (s == null) return null
        return try {
            val bytes = s.toByteArray(Charsets.ISO_8859_1)
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            s
        }
    }
}
