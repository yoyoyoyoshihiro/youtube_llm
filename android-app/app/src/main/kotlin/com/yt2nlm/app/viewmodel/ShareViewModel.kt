package com.yt2nlm.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yt2nlm.app.database.AppDatabase
import com.yt2nlm.app.repository.HistoryRepository
import com.yt2nlm.app.utils.YouTubeUrlValidator
import kotlinx.coroutines.launch

class ShareViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: HistoryRepository
    
    private val _processingState = MutableLiveData<ProcessingState>()
    val processingState: LiveData<ProcessingState> = _processingState
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    enum class ProcessingState {
        VALIDATING,
        LAUNCHING,
        SUCCESS,
        ERROR
    }
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = HistoryRepository(database.historyDao())
        _processingState.value = ProcessingState.VALIDATING
    }
    
    fun setProcessingState(state: ProcessingState) {
        _processingState.value = state
    }
    
    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }
    
    fun setSuccessMessage(message: String) {
        _successMessage.value = message
    }
    
    fun addToHistory(url: String) {
        viewModelScope.launch {
            try {
                val urlInfo = YouTubeUrlValidator.validateAndAnalyze(url)
                if (urlInfo.isValid) {
                    repository.addHistory(
                        url = url,
                        normalizedUrl = urlInfo.normalizedUrl ?: url,
                        videoId = urlInfo.videoId,
                        title = urlInfo.title
                    )
                }
            } catch (e: Exception) {
                // 履歴追加に失敗してもメイン機能には影響しないため、エラーは無視
            }
        }
    }
    
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}