# YouTube→NotebookLM Android アプリ開発計画

## 🎯 アプリ概要

### アプリ名
**YT2NLM** (YouTube to NotebookLM)

### 核心機能
1. **Share Target対応** - YouTube共有候補に確実表示
2. **Intent処理** - YouTube URLを受け取り
3. **NotebookLM起動** - アプリ優先、Web版フォールバック
4. **履歴管理** - 処理したURL履歴
5. **設定機能** - 起動方法のカスタマイズ

## 🔧 技術仕様

### 開発環境
- **プラットフォーム**: Android (API 21+)
- **言語**: Kotlin
- **IDE**: Android Studio
- **ビルドツール**: Gradle

### 必要な権限
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```

### Share Target設定
```xml
<activity android:name=".ShareActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

## 📱 アプリ構成

### 1. メインアクティビティ
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 履歴表示
        // 設定画面
        // 手動URL入力
    }
}
```

### 2. Share処理アクティビティ
```kotlin
class ShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null && isYouTubeUrl(sharedText)) {
            processYouTubeUrl(sharedText)
        }
    }
    
    private fun processYouTubeUrl(url: String) {
        // 1. URLを履歴に保存
        // 2. NotebookLMアプリ起動試行
        // 3. 失敗時はWeb版起動
        // 4. 成功メッセージ表示
    }
}
```

### 3. NotebookLM起動処理
```kotlin
class NotebookLMLauncher {
    companion object {
        private const val NOTEBOOKLM_PACKAGE = "com.google.android.apps.labs.language.tailwind"
        private const val NOTEBOOKLM_WEB = "https://notebooklm.google.com/"
        
        fun launchNotebookLM(context: Context, youtubeUrl: String) {
            // クリップボードにURLコピー
            copyToClipboard(context, youtubeUrl)
            
            // アプリ起動試行
            if (isAppInstalled(context, NOTEBOOKLM_PACKAGE)) {
                launchApp(context, NOTEBOOKLM_PACKAGE)
            } else {
                // Web版起動
                launchWebVersion(context)
            }
        }
    }
}
```

## 🎨 UI/UX設計

### メイン画面
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">
    
    <!-- ロゴ -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="🔗📚"
        android:textSize="48sp"
        android:layout_gravity="center"
        android:layout_marginBottom="16dp" />
    
    <!-- タイトル -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="YouTube→NotebookLM"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_gravity="center"
        android:layout_marginBottom="32dp" />
    
    <!-- 手動URL入力 -->
    <EditText
        android:id="@+id/urlEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="YouTube URLを入力"
        android:inputType="textUri"
        android:layout_marginBottom="16dp" />
    
    <!-- 実行ボタン -->
    <Button
        android:id="@+id/processButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="🚀 NotebookLMで開く"
        android:textSize="16sp"
        android:padding="16dp"
        android:layout_marginBottom="32dp" />
    
    <!-- 履歴リスト -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="📋 処理履歴"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />
    
    <RecyclerView
        android:id="@+id/historyRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
    
</LinearLayout>
```

### Share処理画面
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="24dp"
    android:gravity="center">
    
    <!-- 処理中表示 -->
    <ProgressBar
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp" />
    
    <TextView
        android:id="@+id/statusText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="NotebookLMを起動中..."
        android:textSize="16sp"
        android:layout_marginBottom="16dp" />
    
    <!-- キャンセルボタン -->
    <Button
        android:id="@+id/cancelButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="キャンセル" />
    
</LinearLayout>
```

## 🔧 実装手順

### Phase 1: 基本機能
1. **プロジェクト作成**
2. **Share Target設定**
3. **YouTube URL検証**
4. **NotebookLM起動処理**

### Phase 2: UI/UX改善
1. **メイン画面実装**
2. **履歴機能**
3. **設定画面**
4. **エラーハンドリング**

### Phase 3: 最適化
1. **パフォーマンス改善**
2. **バッテリー最適化**
3. **テスト自動化**
4. **リリース準備**

## 📦 配布方法

### 1. Google Play Store
- **正式配布**: 最も確実
- **審査必要**: 1-2週間
- **有料アカウント**: $25

### 2. APK直接配布
- **即座配布**: GitHub Releasesで
- **インストール**: 提供元不明アプリ許可
- **更新**: 手動

### 3. F-Droid
- **オープンソース**: 無料配布
- **審査**: コミュニティベース
- **信頼性**: OSS愛好者向け

## 💰 開発コスト

### 開発時間
- **Phase 1**: 2-3日
- **Phase 2**: 3-4日  
- **Phase 3**: 2-3日
- **合計**: 約1週間

### 必要リソース
- **Android Studio**: 無料
- **開発者アカウント**: $25 (Google Play)
- **テスト端末**: 既存のAndroid端末
- **デザインツール**: 無料 (Figma Community)

## 🚀 実装開始

```bash
# プロジェクト作成
# Android Studio > New Project > Empty Activity

# 依存関係追加 (build.gradle)
implementation 'androidx.recyclerview:recyclerview:1.3.0'
implementation 'androidx.room:room-runtime:2.4.0'
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0'

# 権限設定 (AndroidManifest.xml)
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />

# Share Target設定
<activity android:name=".ShareActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

## 🎯 実装するか判断

### メリット
✅ 共有候補に確実表示
✅ システム権限で確実起動
✅ ユーザー体験最高
✅ 履歴・設定機能
✅ オフライン対応

### デメリット
❌ 開発時間が必要
❌ Play Store審査
❌ 複数端末テスト
❌ 継続メンテナンス

**実装を開始しますか？**