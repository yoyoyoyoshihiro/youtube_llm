# NotebookLM連携仕様

## URL受け渡し方法

### 方法1: Intent経由（推奨）
```javascript
// NotebookLMアプリへのIntent送信
Intent intent = new Intent(Intent.ACTION_SEND);
intent.setType("text/plain");
intent.putExtra(Intent.EXTRA_TEXT, youtubeUrl);
intent.setPackage("com.google.android.apps.notebook");
startActivity(intent);
```

### 方法2: クリップボード経由（フォールバック）
```javascript
// URLをクリップボードに保存してからNotebookLM起動
ClipboardManager clipboard = getSystemService(ClipboardManager.class);
ClipData clip = ClipData.newPlainText("YouTube URL", youtubeUrl);
clipboard.setPrimaryClip(clip);

// NotebookLM起動
Intent intent = new Intent("android.intent.action.MAIN");
intent.setComponent(new ComponentName("com.google.android.apps.notebook", 
                   "com.google.android.apps.notebook.MainActivity"));
startActivity(intent);
```

## Tasker実装

### NotebookLM起動アクション
```xml
<!-- アプリ起動 -->
<Action sr="act1" ve="7">
    <code>104</code>
    <label>Launch NotebookLM</label>
    <Str sr="arg0" ve="3">com.google.android.apps.notebook</Str>
    <Str sr="arg1" ve="3"/>
    <Int sr="arg5" val="0"/>
</Action>

<!-- Intent送信 -->
<Action sr="act2" ve="7">
    <code>124</code>
    <label>Send Intent</label>
    <Str sr="arg0" ve="3">android.intent.action.SEND</Str>
    <Str sr="arg1" ve="3"/>
    <Str sr="arg2" ve="3">text/plain</Str>
    <Str sr="arg3" ve="3">android.intent.extra.TEXT</Str>
    <Str sr="arg4" ve="3">%youtube_url</Str>
    <Str sr="arg5" ve="3">com.google.android.apps.notebook</Str>
</Action>
```

## エラーハンドリング

### NotebookLMアプリ未インストール対応
```javascript
try {
    // NotebookLM起動試行
    startActivity(notebookLMIntent);
} catch (ActivityNotFoundException e) {
    // Play Storeでインストールページを開く
    Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
    playStoreIntent.setData(Uri.parse("market://details?id=com.google.android.apps.notebook"));
    startActivity(playStoreIntent);
}
```

### URL形式変換
```javascript
// YouTube URLの正規化
function normalizeYouTubeUrl(url) {
    // youtu.be → youtube.com 変換
    if (url.includes("youtu.be/")) {
        const videoId = url.split("youtu.be/")[1].split("?")[0];
        return `https://www.youtube.com/watch?v=${videoId}`;
    }
    
    // モバイル版 → デスクトップ版
    if (url.includes("m.youtube.com")) {
        return url.replace("m.youtube.com", "www.youtube.com");
    }
    
    return url;
}
```

## セキュリティ考慮

### URL検証
- NotebookLMに送信前に再度URL検証
- 不正なパラメータの除去
- プライベート情報の検出と除外

### データ保護
- Intent送信後は変数を即座にクリア
- 送信失敗時のロールバック処理
- エラーログにURLを含めない

## ユーザビリティ改善

### 起動時間最適化
- NotebookLMアプリの事前ウォームアップ
- バックグラウンドでのアプリ状態確認
- 起動失敗時の代替手段提供

### フィードバック
- 成功/失敗の通知表示
- 進行状況インジケーター
- エラー時の分かりやすいメッセージ