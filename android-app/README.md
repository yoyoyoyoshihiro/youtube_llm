# YT2NLM - YouTube→NotebookLM Android App

## 🎯 アプリ概要

YouTube動画のURLを簡単にNotebookLMで開くためのAndroidアプリです。
YouTubeの共有メニューから直接NotebookLMを起動できます。

## 🚀 主な機能

### 1. Share Target機能
- YouTube共有候補に確実に表示
- ワンタップでNotebookLM起動
- アプリ優先、Web版フォールバック

### 2. メインアプリ機能
- 手動URL入力
- クリップボードからのペースト
- 処理履歴表示
- 設定カスタマイズ

### 3. 確実な起動処理
- NotebookLMアプリ検出・起動
- 失敗時のWeb版自動起動
- Custom Tabs対応

## 🛠️ 技術仕様

### 開発環境
- **言語**: Kotlin
- **最小SDK**: API 21 (Android 5.0)
- **対象SDK**: API 34 (Android 14)
- **アーキテクチャ**: MVVM + Repository Pattern

### 主要ライブラリ
- AndroidX Core & AppCompat
- Material Components
- Room Database
- Lifecycle Components
- Custom Tabs

### 権限
- `INTERNET` - Web版起動用
- `QUERY_ALL_PACKAGES` - アプリ検出用

## 📱 アプリ構成

```
app/
├── MainActivity.kt          # メイン画面
├── ShareActivity.kt         # Share処理画面
├── SettingsActivity.kt      # 設定画面
├── utils/
│   ├── NotebookLMLauncher.kt    # NotebookLM起動処理
│   └── YouTubeUrlValidator.kt   # YouTube URL検証
├── model/
│   └── HistoryItem.kt           # 履歴データモデル
├── database/
│   ├── AppDatabase.kt           # Room Database
│   └── HistoryDao.kt            # 履歴データアクセス
├── viewmodel/
│   ├── MainViewModel.kt         # メイン画面ViewModel
│   └── ShareViewModel.kt        # Share処理ViewModel
└── adapter/
    └── HistoryAdapter.kt        # 履歴リストアダプター
```

## 🔧 ビルド手順

### 1. 環境準備
```bash
# Android Studio Arctic Fox以降をインストール
# JDK 8以上が必要
```

### 2. プロジェクトクローン
```bash
git clone <repository-url>
cd yt2nlm-android
```

### 3. ビルド
```bash
./gradlew assembleDebug
```

### 4. インストール
```bash
./gradlew installDebug
```

## 📋 使用方法

### 1. 通常の使用
1. アプリを起動
2. YouTube URLを入力またはペースト
3. 「NotebookLMで開く」をタップ
4. NotebookLMが起動し、URLがクリップボードにコピー済み

### 2. Share Target使用
1. YouTubeアプリで動画を視聴
2. 「共有」ボタンをタップ
3. 「NotebookLMで開く」を選択
4. 自動でNotebookLMが起動

## 🎨 UI/UX特徴

### デザイン
- Material Design 3準拠
- ダークモード対応
- アクセシビリティ配慮

### ユーザー体験
- 処理状況の明確な表示
- エラーハンドリング
- 直感的な操作

## 🔍 URL検証

### 対応URL形式
- `https://www.youtube.com/watch?v=VIDEO_ID`
- `https://youtu.be/VIDEO_ID`
- `https://www.youtube.com/shorts/VIDEO_ID`
- `https://m.youtube.com/watch?v=VIDEO_ID`
- プレイリストURL（v=パラメータ付き）
- 時間指定URL

### 検証機能
- リアルタイム検証
- URL正規化
- ビデオID抽出
- URL種類判定

## 💾 データ管理

### 履歴機能
- 処理したURL履歴
- タイムスタンプ記録
- 再実行機能
- 履歴クリア

### 設定
- 起動方法選択
- クリップボードコピー設定
- 履歴表示設定

## 🛡️ セキュリティ

### プライバシー
- ローカルデータのみ
- 外部サーバー通信なし
- URL履歴は端末内のみ

### 権限の最小化
- 必要最小限の権限のみ
- ランタイム権限対応

## 🧪 テスト

### 単体テスト
```bash
./gradlew test
```

### UI テスト
```bash
./gradlew connectedAndroidTest
```

### 主要テストケース
- YouTube URL検証
- NotebookLM起動処理
- Share Target機能
- 履歴管理
- エラーハンドリング

## 📦 リリース準備

### 1. リリースビルド
```bash
./gradlew assembleRelease
```

### 2. APK署名
```bash
# キーストア作成
keytool -genkey -v -keystore yt2nlm-release.keystore -alias yt2nlm -keyalg RSA -keysize 2048 -validity 10000

# APK署名
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore yt2nlm-release.keystore app-release-unsigned.apk yt2nlm
```

### 3. Play Store準備
- アプリアイコン (512x512)
- スクリーンショット
- アプリ説明文
- プライバシーポリシー

## 📝 バージョン履歴

### v1.0.0
- Share Target機能実装
- YouTube URL検証・正規化
- NotebookLM起動処理
- 履歴管理機能
- 設定画面

## 🤝 貢献方法

1. Forkしてブランチ作成
2. 機能実装・バグ修正
3. テスト実行
4. Pull Request作成

## 📄 ライセンス

MIT License

## 📞 サポート

- GitHub Issues
- バグ報告・機能要望歓迎

## 🎯 今後の計画

- [ ] ウィジェット機能
- [ ] 通知機能
- [ ] テーマカスタマイズ
- [ ] 多言語対応
- [ ] バックアップ・復元機能