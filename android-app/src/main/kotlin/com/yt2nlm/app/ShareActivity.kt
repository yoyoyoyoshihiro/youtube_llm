package com.yt2nlm.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.yt2nlm.app.databinding.ActivityShareBinding
import com.yt2nlm.app.utils.NotebookLMLauncher
import com.yt2nlm.app.utils.YouTubeUrlValidator
import com.yt2nlm.app.viewmodel.ShareViewModel

class ShareActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityShareBinding
    private lateinit var viewModel: ShareViewModel
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // ViewModelの初期化
        viewModel = ViewModelProvider(this)[ShareViewModel::class.java]
        
        // UI初期化
        setupUI()
        setupObservers()
        
        // 共有されたURLを処理
        handleSharedUrl()
    }
    
    private fun setupUI() {
        // キャンセルボタン
        binding.cancelButton.setOnClickListener {
            finish()
        }
        
        // 再試行ボタン
        binding.retryButton.setOnClickListener {
            retryProcessing()
        }
        
        // 閉じるボタン
        binding.closeButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupObservers() {
        // 処理状態の監視
        viewModel.processingState.observe(this) { state ->
            updateUI(state)
        }
        
        // エラーメッセージの監視
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                showError(it)
            }
        }
        
        // 成功メッセージの監視
        viewModel.successMessage.observe(this) { message ->
            message?.let {
                showSuccess(it)
            }
        }
    }
    
    private fun handleSharedUrl() {
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (sharedText != null) {
                    processSharedText(sharedText)
                } else {
                    showError("共有データが見つかりません")
                }
            }
            else -> {
                showError("サポートされていない操作です")
            }
        }
    }
    
    private fun processSharedText(text: String) {
        viewModel.setProcessingState(ShareViewModel.ProcessingState.VALIDATING)
        
        // URL表示
        binding.urlText.text = text
        
        // URL検証
        if (!YouTubeUrlValidator.isValidYouTubeUrl(text)) {
            viewModel.setProcessingState(ShareViewModel.ProcessingState.ERROR)
            viewModel.setErrorMessage(getString(R.string.share_invalid_url))
            return
        }
        
        // 正規化
        val normalizedUrl = YouTubeUrlValidator.normalizeYouTubeUrl(text)
        binding.urlText.text = normalizedUrl
        
        // 履歴に追加
        viewModel.addToHistory(normalizedUrl)
        
        // NotebookLMを起動
        viewModel.setProcessingState(ShareViewModel.ProcessingState.LAUNCHING)
        
        NotebookLMLauncher.launchNotebookLM(
            context = this,
            youtubeUrl = normalizedUrl,
            onSuccess = { method ->
                viewModel.setProcessingState(ShareViewModel.ProcessingState.SUCCESS)
                viewModel.setSuccessMessage("NotebookLMを起動しました ($method)")
                
                // 3秒後に自動で閉じる
                handler.postDelayed({
                    if (!isFinishing) {
                        finish()
                    }
                }, 3000)
            },
            onError = { error ->
                viewModel.setProcessingState(ShareViewModel.ProcessingState.ERROR)
                viewModel.setErrorMessage(error)
            }
        )
    }
    
    private fun retryProcessing() {
        val url = binding.urlText.text.toString()
        if (url.isNotEmpty()) {
            processSharedText(url)
        }
    }
    
    private fun updateUI(state: ShareViewModel.ProcessingState) {
        when (state) {
            ShareViewModel.ProcessingState.VALIDATING -> {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.statusText.text = "YouTube URLを検証中..."
                binding.retryButton.visibility = android.view.View.GONE
                binding.successContainer.visibility = android.view.View.GONE
            }
            
            ShareViewModel.ProcessingState.LAUNCHING -> {
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.statusText.text = getString(R.string.share_processing)
                binding.retryButton.visibility = android.view.View.GONE
                binding.successContainer.visibility = android.view.View.GONE
            }
            
            ShareViewModel.ProcessingState.SUCCESS -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.statusText.visibility = android.view.View.GONE
                binding.cancelButton.visibility = android.view.View.GONE
                binding.retryButton.visibility = android.view.View.GONE
                binding.successContainer.visibility = android.view.View.VISIBLE
            }
            
            ShareViewModel.ProcessingState.ERROR -> {
                binding.progressBar.visibility = android.view.View.GONE
                binding.statusText.text = getString(R.string.share_error)
                binding.retryButton.visibility = android.view.View.VISIBLE
                binding.successContainer.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun showError(message: String) {
        binding.statusText.text = message
        binding.statusText.setTextColor(getColor(R.color.error_text))
    }
    
    private fun showSuccess(message: String) {
        binding.successText.text = message
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}