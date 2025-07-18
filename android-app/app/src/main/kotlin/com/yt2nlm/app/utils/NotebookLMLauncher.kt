package com.yt2nlm.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NotebookLMLauncher {
    
    private const val NOTEBOOKLM_PACKAGE_NAME = "com.google.android.apps.labs.language.tailwind"
    private const val NOTEBOOKLM_WEB_URL = "https://notebooklm.google.com/"
    
    fun launchNotebookLM(
        context: Context,
        youtubeUrl: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: URLをクリップボードにコピー
                copyToClipboard(context, youtubeUrl)
                
                // Step 2: NotebookLMアプリの起動を試行
                val appLaunchResult = tryLaunchApp(context)
                
                withContext(Dispatchers.Main) {
                    if (appLaunchResult) {
                        onSuccess("アプリ")
                    } else {
                        // Step 3: アプリが失敗した場合はWeb版を起動
                        launchWebVersion(context, onSuccess, onError)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("処理中にエラーが発生しました: ${e.message}")
                }
            }
        }
    }
    
    private fun copyToClipboard(context: Context, text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("YouTube URL", text)
        clipboardManager.setPrimaryClip(clipData)
    }
    
    private suspend fun tryLaunchApp(context: Context): Boolean {
        return try {
            if (isAppInstalled(context, NOTEBOOKLM_PACKAGE_NAME)) {
                // アプリ起動Intent
                val appIntent = Intent().apply {
                    action = Intent.ACTION_VIEW
                    setPackage(NOTEBOOKLM_PACKAGE_NAME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                
                context.startActivity(appIntent)
                
                // アプリが起動するまで少し待つ
                delay(1000)
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    private fun launchWebVersion(
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Custom Tabsを試行
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .build()
            
            customTabsIntent.launchUrl(context, Uri.parse(NOTEBOOKLM_WEB_URL))
            onSuccess("Web版 (Custom Tabs)")
            
        } catch (e: Exception) {
            try {
                // フォールバック: 標準ブラウザで開く
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(NOTEBOOKLM_WEB_URL)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                onSuccess("Web版 (ブラウザ)")
                
            } catch (e2: Exception) {
                onError("NotebookLMを開くことができませんでした")
            }
        }
    }
    
    fun isNotebookLMAppInstalled(context: Context): Boolean {
        return isAppInstalled(context, NOTEBOOKLM_PACKAGE_NAME)
    }
    
    fun getNotebookLMAppVersion(context: Context): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(NOTEBOOKLM_PACKAGE_NAME, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            null
        }
    }
}