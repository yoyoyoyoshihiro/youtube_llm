package com.yt2nlm.app.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.yt2nlm.app.model.HistoryItem

@Dao
interface HistoryDao {
    
    @Query("SELECT * FROM history_items ORDER BY lastUsed DESC")
    fun getAllHistory(): LiveData<List<HistoryItem>>
    
    @Query("SELECT * FROM history_items ORDER BY lastUsed DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): LiveData<List<HistoryItem>>
    
    @Query("SELECT * FROM history_items WHERE normalizedUrl = :url LIMIT 1")
    suspend fun getHistoryByUrl(url: String): HistoryItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyItem: HistoryItem): Long
    
    @Update
    suspend fun updateHistory(historyItem: HistoryItem)
    
    @Delete
    suspend fun deleteHistory(historyItem: HistoryItem)
    
    @Query("DELETE FROM history_items")
    suspend fun clearAllHistory()
    
    @Query("SELECT COUNT(*) FROM history_items")
    suspend fun getHistoryCount(): Int
    
    @Query("SELECT * FROM history_items WHERE url LIKE :searchQuery OR normalizedUrl LIKE :searchQuery ORDER BY lastUsed DESC")
    fun searchHistory(searchQuery: String): LiveData<List<HistoryItem>>
    
    @Query("DELETE FROM history_items WHERE timestamp < :cutoffDate")
    suspend fun deleteOldHistory(cutoffDate: java.util.Date)
    
    @Query("SELECT * FROM history_items ORDER BY successCount DESC, lastUsed DESC LIMIT :limit")
    fun getMostUsedHistory(limit: Int): LiveData<List<HistoryItem>>
}