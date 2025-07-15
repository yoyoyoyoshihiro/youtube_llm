# Tasker手動セットアップ手順

XMLインポートが失敗する場合は、手動でプロファイルを作成できます。

## プロファイル1: YouTube URL検出

### 1. 新しいプロファイル作成
```
1. Taskerを開く
2. 「PROFILES」タブを選択
3. 右下の「+」ボタンをタップ
4. 「State」を選択
5. 「Variable」→「Variable Value」を選択
6. Variable: %CLIP
7. Op: Matches Regex
8. Value: .*youtube.*
9. プロファイル名: 「YouTube URL Detection」
```

### 3. タスク作成
```
1. 「New Task」をタップ
2. タスク名: 「YouTube URL Validator」
3. 「✓」をタップしてタスク作成
```

### 4. アクション追加

#### アクション1: クリップボード取得
```
1. 右下の「+」をタップ
2. 「Variables」→「Variable Set」
3. Name: %clipboard_content
4. To: %CLIP
5. 「✓」で確定
```

#### アクション2: YouTube URL検証
```
1. 右下の「+」をタップ
2. 「Variables」→「Variable Search Replace」
3. Variable: %clipboard_content
4. Search: ^https://(www\.|m\.)?youtube\.com/(watch\?v=|shorts/)[a-zA-Z0-9_-]+.*$|^https://youtu\.be/[a-zA-Z0-9_-]+.*$
5. Check「Use Regex」
6. Store Matches In: %is_youtube_url
7. 「✓」で確定
```

#### アクション3: 条件分岐
```
1. 右下の「+」をタップ
2. 「Task」→「If」
3. Condition: %is_youtube_url Set
4. 「✓」で確定
```

#### アクション4: 通知表示
```
1. 右下の「+」をタップ
2. 「Alert」→「Notify」
3. Title: YouTube URL検出
4. Text: NotebookLMで開く？
5. Icon: 17 (リンクアイコン)
6. Actions: 開く,無視
7. ID: youtube_notification
8. Permanent: チェック
9. 「✓」で確定
```

#### アクション5: 変数クリア
```
1. 右下の「+」をタップ
2. 「Variables」→「Variable Clear」
3. Name: %clipboard_content
4. 「✓」で確定
```

#### アクション6: If終了
```
1. 右下の「+」をタップ
2. 「Task」→「End If」
3. 「✓」で確定
```

## プロファイル2: 通知アクション処理

### 1. 新しいプロファイル作成
```
1. 「PROFILES」タブで右下の「+」
2. 「Event」→「UI」→「Notification Click」
3. Owner Application: Tasker
4. Title: YouTube URL検出
```

### 2. アクションタスク作成
```
タスク名: Notification Actions
```

### 3. アクション追加

#### アクション1: アクション判定
```
1. 「Task」→「If」
2. Condition: %nr_action ~ 開く
3. 「✓」で確定
```

#### アクション2: NotebookLM起動
```
1. 「App」→「Launch App」
2. App: NotebookLM (com.google.android.apps.notebook)
3. 「✓」で確定
```

#### アクション3: Intent送信
```
1. 「System」→「Send Intent」
2. Action: android.intent.action.SEND
3. Cat: Default
4. Mime Type: text/plain
5. Extra: android.intent.extra.TEXT:%CLIP
6. Package: com.google.android.apps.notebook
7. 「✓」で確定
```

#### アクション4: If終了
```
1. 「Task」→「End If」
2. 「✓」で確定
```

## 動作テスト

### 1. テスト実行
```
1. YouTubeアプリでテスト動画を開く
2. 共有→「リンクをコピー」
3. 通知が表示されることを確認
4. 「開く」をタップ
5. NotebookLMが起動することを確認
```

### 2. トラブルシューティング
```
通知が表示されない場合:
- プロファイルが有効になっているか確認
- クリップボード権限が許可されているか確認

NotebookLMが起動しない場合:
- アプリがインストールされているか確認
- Intent設定を再確認
```

このステップで手動作成できます。各ステップで問題があれば教えてください。