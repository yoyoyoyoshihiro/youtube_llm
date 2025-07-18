package com.yt2nlm.app.utils

import android.net.Uri
import java.util.regex.Pattern

object YouTubeUrlValidator {
    
    private val YOUTUBE_URL_PATTERNS = listOf(
        // 標準的なYouTube URL
        Pattern.compile("^https?://(www\\.)?youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)"),
        Pattern.compile("^https?://(www\\.)?youtube\\.com/watch\\?.*[&?]v=([a-zA-Z0-9_-]+)"),
        
        // YouTube Shorts
        Pattern.compile("^https?://(www\\.)?youtube\\.com/shorts/([a-zA-Z0-9_-]+)"),
        
        // youtu.be短縮URL
        Pattern.compile("^https?://youtu\\.be/([a-zA-Z0-9_-]+)"),
        
        // モバイルYouTube
        Pattern.compile("^https?://m\\.youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+)"),
        Pattern.compile("^https?://m\\.youtube\\.com/watch\\?.*[&?]v=([a-zA-Z0-9_-]+)"),
        
        // 埋め込みURL
        Pattern.compile("^https?://(www\\.)?youtube\\.com/embed/([a-zA-Z0-9_-]+)"),
        
        // プレイリスト内の動画
        Pattern.compile("^https?://(www\\.)?youtube\\.com/watch\\?.*[&?]v=([a-zA-Z0-9_-]+).*[&?]list=([a-zA-Z0-9_-]+)"),
        
        // 時間指定付きURL
        Pattern.compile("^https?://(www\\.)?youtube\\.com/watch\\?v=([a-zA-Z0-9_-]+).*[&?]t=([0-9]+)"),
        Pattern.compile("^https?://youtu\\.be/([a-zA-Z0-9_-]+)\\?t=([0-9]+)")
    )
    
    private val ALLOWED_DOMAINS = setOf(
        "youtube.com",
        "www.youtube.com",
        "m.youtube.com",
        "youtu.be"
    )
    
    /**
     * URLがYouTubeの動画URLかどうかを検証
     */
    fun isValidYouTubeUrl(url: String): Boolean {
        if (url.isBlank()) return false
        
        try {
            val uri = Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false
            
            // ドメインチェック
            if (!ALLOWED_DOMAINS.contains(host)) {
                return false
            }
            
            // パターンマッチング
            return YOUTUBE_URL_PATTERNS.any { pattern ->
                pattern.matcher(url).find()
            }
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * YouTube URLを正規化（標準形式に変換）
     */
    fun normalizeYouTubeUrl(url: String): String {
        if (!isValidYouTubeUrl(url)) {
            return url
        }
        
        try {
            val videoId = extractVideoId(url)
            if (videoId != null) {
                return "https://www.youtube.com/watch?v=$videoId"
            }
        } catch (e: Exception) {
            // 正規化に失敗した場合は元のURLを返す
        }
        
        return url
    }
    
    /**
     * YouTube URLからビデオIDを抽出
     */
    fun extractVideoId(url: String): String? {
        for (pattern in YOUTUBE_URL_PATTERNS) {
            val matcher = pattern.matcher(url)
            if (matcher.find()) {
                // 最初のキャプチャグループがビデオIDの場合が多い
                return matcher.group(2) ?: matcher.group(1)
            }
        }
        return null
    }
    
    /**
     * YouTube URLからタイトルを推測（可能な場合）
     */
    fun extractTitleFromUrl(url: String): String? {
        try {
            val uri = Uri.parse(url)
            
            // URLパラメータからタイトルを探す
            val title = uri.getQueryParameter("title") 
                ?: uri.getQueryParameter("t")
                ?: uri.getQueryParameter("text")
            
            return title?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * URLの種類を判定
     */
    fun getUrlType(url: String): YouTubeUrlType {
        if (!isValidYouTubeUrl(url)) {
            return YouTubeUrlType.INVALID
        }
        
        return when {
            url.contains("/shorts/") -> YouTubeUrlType.SHORTS
            url.contains("youtu.be/") -> YouTubeUrlType.SHORT_URL
            url.contains("/embed/") -> YouTubeUrlType.EMBED
            url.contains("list=") -> YouTubeUrlType.PLAYLIST
            url.contains("t=") -> YouTubeUrlType.TIMESTAMPED
            else -> YouTubeUrlType.STANDARD
        }
    }
    
    /**
     * URLを検証し、詳細情報を返す
     */
    fun validateAndAnalyze(url: String): YouTubeUrlInfo {
        return YouTubeUrlInfo(
            originalUrl = url,
            isValid = isValidYouTubeUrl(url),
            normalizedUrl = if (isValidYouTubeUrl(url)) normalizeYouTubeUrl(url) else null,
            videoId = extractVideoId(url),
            urlType = getUrlType(url),
            title = extractTitleFromUrl(url)
        )
    }
}

/**
 * YouTube URLの種類
 */
enum class YouTubeUrlType {
    INVALID,
    STANDARD,
    SHORTS,
    SHORT_URL,
    EMBED,
    PLAYLIST,
    TIMESTAMPED
}

/**
 * YouTube URL情報
 */
data class YouTubeUrlInfo(
    val originalUrl: String,
    val isValid: Boolean,
    val normalizedUrl: String?,
    val videoId: String?,
    val urlType: YouTubeUrlType,
    val title: String?
)