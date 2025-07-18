package com.yt2nlm.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val url: String,
    val normalizedUrl: String,
    val videoId: String?,
    val title: String?,
    val timestamp: Date,
    val successCount: Int = 0,
    val lastUsed: Date = timestamp
) {
    companion object {
        fun create(
            url: String,
            normalizedUrl: String = url,
            videoId: String? = null,
            title: String? = null
        ): HistoryItem {
            val now = Date()
            return HistoryItem(
                url = url,
                normalizedUrl = normalizedUrl,
                videoId = videoId,
                title = title,
                timestamp = now,
                lastUsed = now
            )
        }
    }
    
    fun incrementUsage(): HistoryItem {
        return copy(
            successCount = successCount + 1,
            lastUsed = Date()
        )
    }
    
    fun getDisplayTitle(): String {
        return title ?: extractTitleFromUrl() ?: "YouTube Video"
    }
    
    private fun extractTitleFromUrl(): String? {
        return try {
            val uri = android.net.Uri.parse(normalizedUrl)
            val videoId = uri.getQueryParameter("v")
            if (videoId != null) {
                "YouTube Video ($videoId)"
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun getDisplayUrl(): String {
        return if (normalizedUrl.length > 50) {
            "${normalizedUrl.substring(0, 47)}..."
        } else {
            normalizedUrl
        }
    }
}