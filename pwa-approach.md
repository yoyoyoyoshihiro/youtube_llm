# PWA（Progressive Web App）アプローチ

## 📱 ユーザー体験

### 導入（30秒で完了）
```
1. 専用URLにアクセス
2. 「ホーム画面に追加」をタップ
3. 完了！
```

### 使用方法
```
1. YouTubeアプリで動画視聴
2. 共有ボタン→「YouTube→NotebookLM」を選択
3. 自動でNotebookLMが起動
```

## 🛠️ 技術実装

### PWAの特徴
- **インストール不要**: Webページとして動作
- **共有ターゲット**: Android共有メニューに表示
- **オフライン対応**: Service Worker活用
- **ネイティブ感**: フルスクリーンで動作

### Web App Manifest
```json
{
  "name": "YouTube→NotebookLM",
  "short_name": "YT→NLM",
  "start_url": "/",
  "display": "standalone",
  "theme_color": "#ff0000",
  "background_color": "#ffffff",
  "icons": [
    {
      "src": "icon.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ],
  "share_target": {
    "action": "/share",
    "method": "GET",
    "params": {
      "text": "url"
    }
  }
}
```

### メイン機能
```javascript
// YouTube URL検証
function isYouTubeURL(url) {
    const patterns = [
        /youtube\.com\/watch\?v=/,
        /youtu\.be\//,
        /youtube\.com\/shorts\//
    ];
    return patterns.some(pattern => pattern.test(url));
}

// NotebookLM起動
function openNotebookLM(url) {
    // Method 1: アプリ起動試行
    const appIntent = `intent://notebook/#Intent;scheme=https;package=com.google.android.apps.notebook;S.text=${encodeURIComponent(url)};end`;
    
    // Method 2: Web版フォールバック
    const webUrl = `https://notebooklm.google.com/?url=${encodeURIComponent(url)}`;
    
    try {
        window.location.href = appIntent;
        // フォールバック用タイマー
        setTimeout(() => {
            window.open(webUrl, '_blank');
        }, 500);
    } catch (e) {
        window.open(webUrl, '_blank');
    }
}
```

## 🔒 セキュリティ設計

### データ保護
- **一時処理**: URLを保存せず即座に処理
- **CSP設定**: Content Security Policy適用
- **HTTPS必須**: セキュア通信のみ
- **サンドボックス**: ブラウザの安全な環境内

### 入力検証
```javascript
function validateAndProcess(url) {
    // 1. YouTube URL検証
    if (!isYouTubeURL(url)) {
        showError("YouTube URLではありません");
        return;
    }
    
    // 2. 機密情報検出
    if (containsSensitiveData(url)) {
        showError("安全でないURLです");
        return;
    }
    
    // 3. 正規化処理
    const cleanUrl = normalizeURL(url);
    
    // 4. NotebookLM起動
    openNotebookLM(cleanUrl);
}
```

## 📊 他手法との比較

| 方法 | 導入時間 | 設定複雑度 | 安定性 | セキュリティ |
|------|---------|-----------|--------|-------------|
| Tasker | 15分 | ★★★★★ | ★★★☆☆ | ★★★★☆ |
| **PWA** | **30秒** | **★☆☆☆☆** | **★★★★★** | **★★★★★** |
| 専用アプリ | 2分 | ★☆☆☆☆ | ★★★★☆ | ★★★★☆ |

## 🚀 実装計画

### Phase 1: 基本PWA
- YouTube URL検証
- NotebookLM起動
- エラーハンドリング

### Phase 2: UI改善
- 進行状況表示
- カスタマイズ設定
- 履歴機能（オプション）

### Phase 3: 高度機能
- バッチ処理
- 要約プレビュー
- オフライン対応

## 💫 メリット

### ユーザー視点
- **超簡単導入**: URLアクセス→追加のみ
- **設定不要**: 複雑な設定一切なし
- **直感的**: 共有メニューから選択
- **安全**: ブラウザの保護環境

### 開発・運用視点
- **更新簡単**: Webページ更新のみ
- **配布楽**: URLシェアのみ
- **デバッグ簡単**: ブラウザ開発者ツール使用
- **プラットフォーム非依存**: iOS/Androidで共通

この方法で作り直しますか？