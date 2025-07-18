package com.yt2nlm.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yt2nlm.app.database.AppDatabase
import com.yt2nlm.app.model.HistoryItem
import com.yt2nlm.app.repository.HistoryRepository
import com.yt2nlm.app.utils.YouTubeUrlValidator
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: HistoryRepository
    
    val historyItems: LiveData<List<HistoryItem>>
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = HistoryRepository(database.historyDao())
        historyItems = repository.getRecentHistory()
    }
    
    fun addHistoryItem(url: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val urlInfo = YouTubeUrlValidator.validateAndAnalyze(url)
                if (urlInfo.isValid) {
                    repository.addHistory(
                        url = url,
                        normalizedUrl = urlInfo.normalizedUrl ?: url,
                        videoId = urlInfo.videoId,
                        title = urlInfo.title
                    )
                    _successMessage.value = "履歴に追加しました"
                } else {
                    _errorMessage.value = "無効なYouTube URLです"
                }
            } catch (e: Exception) {
                _errorMessage.value = "履歴の追加に失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteHistoryItem(historyItem: HistoryItem) {
        viewModelScope.launch {
            try {
                repository.deleteHistory(historyItem)
                _successMessage.value = "履歴を削除しました"
            } catch (e: Exception) {
                _errorMessage.value = "履歴の削除に失敗しました: ${e.message}"
            }
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            try {
                repository.clearAllHistory()
                _successMessage.value = "履歴をクリアしました"
            } catch (e: Exception) {
                _errorMessage.value = "履歴のクリアに失敗しました: ${e.message}"
            }
        }
    }
    
    fun getHistoryCount(callback: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val count = repository.getHistoryCount()
                callback(count)
            } catch (e: Exception) {
                _errorMessage.value = "履歴の取得に失敗しました: ${e.message}"
            }
        }
    }
    
    fun searchHistory(query: String): LiveData<List<HistoryItem>> {
        return repository.searchHistory(query)
    }
    
    fun cleanupOldHistory(daysToKeep: Int = 30) {
        viewModelScope.launch {
            try {
                repository.cleanupOldHistory(daysToKeep)
                _successMessage.value = "古い履歴を削除しました"
            } catch (e: Exception) {
                _errorMessage.value = "履歴のクリーンアップに失敗しました: ${e.message}"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}