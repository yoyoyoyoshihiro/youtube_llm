# Android Studio ビルド手順

## 📱 YT2NLM Android App - 完成版

### 🎯 現在の状況

✅ **完成したコンポーネント**:
- メインアクティビティ (MainActivity.kt)
- 共有処理アクティビティ (ShareActivity.kt)
- 設定画面 (SettingsActivity.kt)
- YouTube URL検証 (YouTubeUrlValidator.kt)
- NotebookLM起動処理 (NotebookLMLauncher.kt)
- 履歴管理 (Room Database)
- 完全なUI/UXレイアウト
- Share Target設定

### 🚀 Android Studio でのビルド

#### 1. Android Studio を起動
```bash
# Android Studio を開く
open -a "Android Studio"
```

#### 2. プロジェクトを開く
1. "Open an existing Android Studio project"
2. この `android-app` フォルダを選択
3. プロジェクトが読み込まれるのを待つ

#### 3. 必要な SDK をインストール
- **Target SDK**: API 34 (Android 14)
- **Min SDK**: API 21 (Android 5.0)
- **Build Tools**: 34.0.0
- **Kotlin**: 1.9.10

#### 4. ビルド実行
```bash
# Debug APK 生成
./gradlew assembleDebug

# Release APK 生成
./gradlew assembleRelease
```

### 📋 手動セットアップ（Android Studio なしの場合）

#### 1. 必要なツール
- **JDK 8+**: `java -version` で確認
- **Android SDK**: `$ANDROID_HOME` 設定
- **Gradle**: 8.0+

#### 2. SDK コンポーネント
```bash
# SDK Manager でインストール
sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "platform-tools"
```

#### 3. 環境変数設定
```bash
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
```

### 🔧 ビルド後の確認

#### 1. APK の場所
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release-unsigned.apk
```

#### 2. インストール
```bash
# Debug APK をインストール
adb install app/build/outputs/apk/debug/app-debug.apk

# デバイスに転送
adb push app-debug.apk /sdcard/
```

### 🎯 アプリの主要機能

#### 1. Share Target 機能
- YouTube アプリで「共有」→「NotebookLMで開く」
- 自動でNotebookLMアプリまたはWeb版起動
- URL自動クリップボードコピー

#### 2. メインアプリ機能
- 手動URL入力
- クリップボードからペースト
- 履歴管理
- 設定カスタマイズ

#### 3. 確実な起動処理
- NotebookLMアプリ検出・起動
- 失敗時のWeb版自動起動
- Custom Tabs対応

### 🏗️ プロジェクト構造

```
android-app/
├── app/
│   ├── build.gradle              # アプリビルド設定
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # アプリマニフェスト
│   │   ├── kotlin/com/yt2nlm/app/
│   │   │   ├── MainActivity.kt    # メイン画面
│   │   │   ├── ShareActivity.kt   # 共有処理
│   │   │   ├── SettingsActivity.kt # 設定画面
│   │   │   ├── utils/
│   │   │   │   ├── NotebookLMLauncher.kt
│   │   │   │   └── YouTubeUrlValidator.kt
│   │   │   ├── model/
│   │   │   │   └── HistoryItem.kt
│   │   │   ├── database/
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── HistoryDao.kt
│   │   │   │   └── Converters.kt
│   │   │   ├── viewmodel/
│   │   │   │   ├── MainViewModel.kt
│   │   │   │   └── ShareViewModel.kt
│   │   │   ├── adapter/
│   │   │   │   └── HistoryAdapter.kt
│   │   │   └── repository/
│   │   │       └── HistoryRepository.kt
│   │   └── res/
│   │       ├── layout/           # UI レイアウト
│   │       ├── values/           # 文字列・色・テーマ
│   │       ├── drawable/         # アイコン・背景
│   │       ├── mipmap-*/         # アプリアイコン
│   │       └── xml/              # 設定・権限
├── build.gradle                 # プロジェクトビルド設定
├── settings.gradle              # プロジェクト設定
└── gradle.properties           # Gradle設定
```

### 🔍 トラブルシューティング

#### 1. ビルドエラー
```bash
# クリーンビルド
./gradlew clean assembleDebug

# 依存関係更新
./gradlew --refresh-dependencies
```

#### 2. SDK エラー
```bash
# Android SDK パスを確認
echo $ANDROID_HOME

# SDK コンポーネントをインストール
sdkmanager --list
```

#### 3. 権限エラー
```bash
# gradlew に実行権限を付与
chmod +x gradlew
```

### 🎉 完成！

このアプリが完成すると：
- ✅ YouTube共有候補に確実表示
- ✅ NotebookLMアプリ自動起動
- ✅ Web版フォールバック
- ✅ 履歴管理
- ✅ 設定カスタマイズ

最初にあった「YouTubeアプリから共有でアプリが出てこない」問題が**完全に解決**されます！

### 📦 配布方法

1. **APK直接配布**: GitHub Releases
2. **Google Play Store**: $25 で正式配布
3. **F-Droid**: オープンソース配布

**次のステップ**: Android Studio でプロジェクトを開いてビルドを実行してください！