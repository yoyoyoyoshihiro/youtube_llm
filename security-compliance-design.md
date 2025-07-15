# セキュリティ・コンプライアンス設計書

## プライバシー保護設計

### データ処理の原則
- **ローカル処理のみ**: すべての処理を端末内で完結
- **一時的処理**: URLの記録・保存は一切行わない
- **最小権限**: 必要最小限の権限のみ要求
- **透明性**: ユーザーが処理内容を完全に把握可能

### クリップボード監視の制限
```
対象URLパターン（厳格に制限）:
- youtube.com/watch?v=*
- youtu.be/*
- m.youtube.com/watch?v=*
- youtube.com/shorts/*

除外条件:
- プライベートな文字列（パスワード、トークン等）
- 個人情報を含む可能性のある文字列
- YouTube以外のドメイン
```

### データ保護
- **暗号化**: 一時的なデータも暗号化して処理
- **自動削除**: 処理完了後即座にメモリから削除
- **ログなし**: 処理履歴やアクセスログは保存しない

## コンプライアンス対応

### 法的考慮事項
- **個人情報保護法**: 個人を特定可能な情報は一切処理しない
- **プラットフォームポリシー**: YouTube/Google ToS準拠
- **アプリストアポリシー**: Google Play規約準拠

### 権限要求の最小化
```
要求権限:
✅ android.permission.READ_EXTERNAL_STORAGE (クリップボード読み取り)
✅ android.permission.WAKE_LOCK (通知表示)
✅ android.permission.INTERNET (NotebookLM起動)

不要権限:
❌ 連絡先アクセス
❌ 位置情報
❌ カメラ・マイク
❌ ストレージ書き込み
```

## ユーザビリティ保護

### 誤作動防止
- **厳密なURL判定**: YouTube URLのみに限定
- **ユーザー選択**: 通知による明示的な同意
- **タイムアウト**: 8秒後の自動消滅
- **無効化機能**: いつでも機能停止可能

### ユーザーコントロール
- **ON/OFF切り替え**: ワンタップで無効化
- **通知設定**: 表示時間・位置のカスタマイズ
- **除外リスト**: 特定チャンネルの除外設定

## セキュリティ実装

### 入力検証
```javascript
function validateYouTubeURL(url) {
    // 厳密なURL検証
    const patterns = [
        /^https:\/\/(?:www\.)?youtube\.com\/watch\?v=[\w-]+(?:&[\w=&]*)?$/,
        /^https:\/\/youtu\.be\/[\w-]+(?:\?[\w=&]*)?$/,
        /^https:\/\/m\.youtube\.com\/watch\?v=[\w-]+(?:&[\w=&]*)?$/,
        /^https:\/\/(?:www\.)?youtube\.com\/shorts\/[\w-]+(?:\?[\w=&]*)?$/
    ];
    
    return patterns.some(pattern => pattern.test(url)) && 
           !containsSensitiveData(url);
}

function containsSensitiveData(url) {
    // 機密情報パターンの検出
    const sensitivePatterns = [
        /password/i, /token/i, /key/i, /auth/i,
        /email/i, /phone/i, /address/i
    ];
    
    return sensitivePatterns.some(pattern => pattern.test(url));
}
```

### エラーハンドリング
- **フェイルセーフ**: エラー時は何もしない
- **ログ制限**: エラーログも個人情報を含まない
- **復旧機能**: 問題発生時の自動復旧

## 監査・検証

### テスト項目
- [ ] 非YouTube URLでの非反応
- [ ] 機密情報含有URLでの非反応  
- [ ] メモリリーク検証
- [ ] 権限スコープ検証
- [ ] プライバシー設定検証

### 継続監視
- 定期的なセキュリティ監査
- ユーザーフィードバック監視
- プラットフォームポリシー変更への対応