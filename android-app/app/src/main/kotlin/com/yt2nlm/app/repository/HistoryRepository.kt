package com.yt2nlm.app.repository

import androidx.lifecycle.LiveData
import com.yt2nlm.app.database.HistoryDao
import com.yt2nlm.app.model.HistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(private val historyDao: HistoryDao) {
    
    fun getAllHistory(): LiveData<List<HistoryItem>> {
        return historyDao.getAllHistory()
    }
    
    fun getRecentHistory(limit: Int = 50): LiveData<List<HistoryItem>> {
        return historyDao.getRecentHistory(limit)
    }
    
    suspend fun addHistory(url: String, normalizedUrl: String, videoId: String?, title: String?): HistoryItem {
        return withContext(Dispatchers.IO) {
            // 既存のアイテムがあるかチェック
            val existingItem = historyDao.getHistoryByUrl(normalizedUrl)
            
            if (existingItem != null) {
                // 既存のアイテムの使用回数を増やす
                val updatedItem = existingItem.incrementUsage()
                historyDao.updateHistory(updatedItem)
                updatedItem
            } else {
                // 新しいアイテムを作成
                val newItem = HistoryItem.create(
                    url = url,
                    normalizedUrl = normalizedUrl,
                    videoId = videoId,
                    title = title
                )
                val id = historyDao.insertHistory(newItem)
                newItem.copy(id = id)
            }
        }
    }
    
    suspend fun deleteHistory(historyItem: HistoryItem) {
        withContext(Dispatchers.IO) {
            historyDao.deleteHistory(historyItem)
        }
    }
    
    suspend fun clearAllHistory() {
        withContext(Dispatchers.IO) {
            historyDao.clearAllHistory()
        }
    }
    
    suspend fun getHistoryCount(): Int {
        return withContext(Dispatchers.IO) {
            historyDao.getHistoryCount()
        }
    }
    
    fun searchHistory(query: String): LiveData<List<HistoryItem>> {
        val searchQuery = "%$query%"
        return historyDao.searchHistory(searchQuery)
    }
    
    suspend fun cleanupOldHistory(daysToKeep: Int = 30) {
        withContext(Dispatchers.IO) {
            val cutoffDate = java.util.Date(System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L))
            historyDao.deleteOldHistory(cutoffDate)
        }
    }
    
    fun getMostUsedHistory(limit: Int = 10): LiveData<List<HistoryItem>> {
        return historyDao.getMostUsedHistory(limit)
    }
}