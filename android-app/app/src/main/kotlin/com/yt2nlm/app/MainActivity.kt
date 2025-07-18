package com.yt2nlm.app

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt2nlm.app.databinding.ActivityMainBinding
import com.yt2nlm.app.model.HistoryItem
import com.yt2nlm.app.utils.NotebookLMLauncher
import com.yt2nlm.app.utils.YouTubeUrlValidator
import com.yt2nlm.app.viewmodel.MainViewModel
import com.yt2nlm.app.adapter.HistoryAdapter

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var clipboardManager: ClipboardManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // ViewModelの初期化
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // ClipboardManagerの初期化
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        // UI初期化
        setupUI()
        setupRecyclerView()
        setupObservers()
    }
    
    private fun setupUI() {
        // ペーストボタン
        binding.pasteButton.setOnClickListener {
            pasteFromClipboard()
        }
        
        // 実行ボタン
        binding.processButton.setOnClickListener {
            processUrl()
        }
        
        // 履歴クリアボタン
        binding.clearHistoryButton.setOnClickListener {
            showClearHistoryDialog()
        }
        
        // 設定メニュー
        setSupportActionBar(binding.toolbar)
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onItemClick = { historyItem ->
                // 履歴アイテムをクリック時の処理
                binding.urlEditText.setText(historyItem.url)
                processUrl()
            },
            onDeleteClick = { historyItem ->
                // 削除ボタンクリック時の処理
                viewModel.deleteHistoryItem(historyItem)
            }
        )
        
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
        }
    }
    
    private fun setupObservers() {
        // 履歴データの監視
        viewModel.historyItems.observe(this) { historyItems ->
            historyAdapter.submitList(historyItems)
            updateHistoryVisibility(historyItems.isEmpty())
        }
        
        // エラーメッセージの監視
        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                showErrorMessage(it)
                viewModel.clearErrorMessage()
            }
        }
        
        // 成功メッセージの監視
        viewModel.successMessage.observe(this) { message ->
            message?.let {
                showSuccessMessage(it)
                viewModel.clearSuccessMessage()
            }
        }
    }
    
    private fun pasteFromClipboard() {
        try {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val pastedText = clipData.getItemAt(0).text?.toString() ?: ""
                if (pastedText.isNotEmpty()) {
                    binding.urlEditText.setText(pastedText)
                    
                    // YouTube URLの場合は自動処理を提案
                    if (YouTubeUrlValidator.isValidYouTubeUrl(pastedText)) {
                        showAutoProcessDialog(pastedText)
                    }
                } else {
                    showErrorMessage(getString(R.string.error_clipboard_empty))
                }
            } else {
                showErrorMessage(getString(R.string.error_clipboard_empty))
            }
        } catch (e: Exception) {
            showErrorMessage(getString(R.string.error_clipboard_no_permission))
        }
    }
    
    private fun processUrl() {
        val url = binding.urlEditText.text.toString().trim()
        
        if (url.isEmpty()) {
            showErrorMessage(getString(R.string.error_no_url))
            return
        }
        
        if (!YouTubeUrlValidator.isValidYouTubeUrl(url)) {
            showErrorMessage(getString(R.string.error_invalid_url))
            return
        }
        
        // 正規化されたURLを取得
        val normalizedUrl = YouTubeUrlValidator.normalizeYouTubeUrl(url)
        
        // 履歴に追加
        viewModel.addHistoryItem(normalizedUrl)
        
        // NotebookLMを起動
        NotebookLMLauncher.launchNotebookLM(
            context = this,
            youtubeUrl = normalizedUrl,
            onSuccess = { method ->
                showSuccessMessage("✅ NotebookLMを起動しました ($method)")
                clearUrlInput()
            },
            onError = { error ->
                showErrorMessage("❌ $error")
            }
        )
    }
    
    private fun showAutoProcessDialog(url: String) {
        AlertDialog.Builder(this)
            .setTitle("YouTube URLを検出")
            .setMessage("このURLをNotebookLMで開きますか？\n\n$url")
            .setPositiveButton("開く") { _, _ ->
                processUrl()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }
    
    private fun showClearHistoryDialog() {
        if (historyAdapter.itemCount == 0) {
            showErrorMessage("履歴がありません")
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle("履歴をクリア")
            .setMessage(getString(R.string.history_clear_confirm))
            .setPositiveButton(getString(R.string.history_clear_yes)) { _, _ ->
                viewModel.clearHistory()
                showSuccessMessage("履歴をクリアしました")
            }
            .setNegativeButton(getString(R.string.history_clear_no), null)
            .show()
    }
    
    private fun updateHistoryVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyHistoryText.visibility = android.view.View.VISIBLE
            binding.historyRecyclerView.visibility = android.view.View.GONE
        } else {
            binding.emptyHistoryText.visibility = android.view.View.GONE
            binding.historyRecyclerView.visibility = android.view.View.VISIBLE
        }
    }
    
    private fun clearUrlInput() {
        binding.urlEditText.setText("")
    }
    
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}