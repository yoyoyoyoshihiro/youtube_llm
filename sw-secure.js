// Service Worker for YouTube→NotebookLM PWA - セキュア版
'use strict';

const CACHE_NAME = 'yt-notebooklm-secure-v1.0.0';
const CACHE_URLS = [
  '/production-pwa.html',
  '/manifest-secure.json'
];

// セキュリティ設定
const SECURITY_CONFIG = {
  ALLOWED_ORIGINS: [
    'https://notebooklm.google.com',
    'https://youtube.com',
    'https://www.youtube.com',
    'https://youtu.be'
  ],
  MAX_CACHE_SIZE: 50 * 1024 * 1024, // 50MB
  CACHE_DURATION: 24 * 60 * 60 * 1000, // 24時間
  MAX_LOG_ENTRIES: 100
};

// セキュリティログ
const securityLogs = [];

function logSecurityEvent(event, details = {}) {
  const logEntry = {
    timestamp: new Date().toISOString(),
    event,
    details,
    cacheVersion: CACHE_NAME
  };
  
  securityLogs.push(logEntry);
  
  // ログサイズ制限
  if (securityLogs.length > SECURITY_CONFIG.MAX_LOG_ENTRIES) {
    securityLogs.shift();
  }
  
  console.info('SW Security Event:', logEntry);
}

// 安全なURL検証
function isSecureRequest(request) {
  try {
    const url = new URL(request.url);
    
    // HTTPS必須（localhost除く）
    if (url.protocol !== 'https:' && url.hostname !== 'localhost') {
      logSecurityEvent('INSECURE_REQUEST_BLOCKED', { url: url.href });
      return false;
    }
    
    // 許可されたOriginチェック
    const isAllowedOrigin = SECURITY_CONFIG.ALLOWED_ORIGINS.some(origin => 
      url.href.startsWith(origin) || url.hostname === self.location.hostname
    );
    
    if (!isAllowedOrigin) {
      logSecurityEvent('UNAUTHORIZED_ORIGIN', { hostname: url.hostname });
      return false;
    }
    
    return true;
  } catch (error) {
    logSecurityEvent('URL_VALIDATION_ERROR', { error: error.message });
    return false;
  }
}

// セキュアキャッシュ処理
async function secureCache(request, response) {
  try {
    // レスポンスサイズチェック
    const responseSize = parseInt(response.headers.get('content-length') || '0');
    if (responseSize > SECURITY_CONFIG.MAX_CACHE_SIZE) {
      logSecurityEvent('CACHE_SIZE_LIMIT_EXCEEDED', { size: responseSize });
      return response;
    }
    
    // Content-Typeチェック
    const contentType = response.headers.get('content-type') || '';
    const allowedTypes = ['text/html', 'text/css', 'text/javascript', 'application/javascript', 'application/json'];
    
    if (!allowedTypes.some(type => contentType.includes(type))) {
      logSecurityEvent('UNSAFE_CONTENT_TYPE', { contentType });
      return response;
    }
    
    // キャッシュに保存
    const cache = await caches.open(CACHE_NAME);
    const responseClone = response.clone();
    
    // キャッシュエントリに有効期限を設定
    const cacheEntry = {
      response: responseClone,
      timestamp: Date.now(),
      expiresAt: Date.now() + SECURITY_CONFIG.CACHE_DURATION
    };
    
    await cache.put(request, responseClone);
    logSecurityEvent('SECURE_CACHE_STORED', { url: request.url });
    
    return response;
  } catch (error) {
    logSecurityEvent('CACHE_ERROR', { error: error.message });
    return response;
  }
}

// インストール時の処理
self.addEventListener('install', event => {
  logSecurityEvent('SERVICE_WORKER_INSTALLING');
  
  event.waitUntil(
    (async () => {
      try {
        const cache = await caches.open(CACHE_NAME);
        
        // 基本ファイルをキャッシュ
        await cache.addAll(CACHE_URLS);
        
        logSecurityEvent('INITIAL_CACHE_COMPLETED', { urls: CACHE_URLS });
        
        // セキュリティヘッダーの追加
        await self.skipWaiting();
        
      } catch (error) {
        logSecurityEvent('INSTALL_ERROR', { error: error.message });
        throw error;
      }
    })()
  );
});

// アクティベーション時の処理
self.addEventListener('activate', event => {
  logSecurityEvent('SERVICE_WORKER_ACTIVATING');
  
  event.waitUntil(
    (async () => {
      try {
        // 古いキャッシュの削除
        const cacheNames = await caches.keys();
        const deletePromises = cacheNames
          .filter(cache => cache.startsWith('yt-notebooklm-') && cache !== CACHE_NAME)
          .map(cache => {
            logSecurityEvent('DELETING_OLD_CACHE', { cacheName: cache });
            return caches.delete(cache);
          });
        
        await Promise.all(deletePromises);
        
        // 期限切れキャッシュのクリーンアップ
        await cleanupExpiredCache();
        
        await self.clients.claim();
        logSecurityEvent('SERVICE_WORKER_ACTIVATED');
        
      } catch (error) {
        logSecurityEvent('ACTIVATION_ERROR', { error: error.message });
        throw error;
      }
    })()
  );
});

// 期限切れキャッシュクリーンアップ
async function cleanupExpiredCache() {
  try {
    const cache = await caches.open(CACHE_NAME);
    const requests = await cache.keys();
    const now = Date.now();
    
    for (const request of requests) {
      // 簡易的な期限チェック（実際は各エントリにタイムスタンプを保存する必要がある）
      const response = await cache.match(request);
      const dateHeader = response.headers.get('date');
      
      if (dateHeader) {
        const responseDate = new Date(dateHeader).getTime();
        if (now - responseDate > SECURITY_CONFIG.CACHE_DURATION) {
          await cache.delete(request);
          logSecurityEvent('EXPIRED_CACHE_REMOVED', { url: request.url });
        }
      }
    }
  } catch (error) {
    logSecurityEvent('CACHE_CLEANUP_ERROR', { error: error.message });
  }
}

// フェッチイベント処理（セキュア版）
self.addEventListener('fetch', event => {
  // セキュリティチェック
  if (!isSecureRequest(event.request)) {
    event.respondWith(
      new Response('Unauthorized', {
        status: 403,
        statusText: 'Forbidden',
        headers: {
          'Content-Type': 'text/plain',
          'X-Content-Type-Options': 'nosniff',
          'X-Frame-Options': 'DENY'
        }
      })
    );
    return;
  }
  
  // GET リクエストのみ処理
  if (event.request.method !== 'GET') {
    return;
  }
  
  // 外部リソースは直接通す
  const url = new URL(event.request.url);
  if (url.hostname !== self.location.hostname) {
    return;
  }
  
  event.respondWith(
    (async () => {
      try {
        // キャッシュ確認
        const cachedResponse = await caches.match(event.request);
        if (cachedResponse) {
          logSecurityEvent('CACHE_HIT', { url: event.request.url });
          
          // キャッシュの有効性確認
          const dateHeader = cachedResponse.headers.get('date');
          if (dateHeader) {
            const cacheAge = Date.now() - new Date(dateHeader).getTime();
            if (cacheAge > SECURITY_CONFIG.CACHE_DURATION) {
              // 期限切れキャッシュの場合はネットワークから取得
              logSecurityEvent('CACHE_EXPIRED', { url: event.request.url });
            } else {
              return cachedResponse;
            }
          } else {
            return cachedResponse;
          }
        }
        
        // ネットワークから取得
        logSecurityEvent('NETWORK_FETCH', { url: event.request.url });
        
        const response = await fetch(event.request);
        
        // レスポンス検証
        if (!response.ok) {
          logSecurityEvent('NETWORK_ERROR', { 
            url: event.request.url, 
            status: response.status 
          });
          
          // キャッシュされたレスポンスをフォールバックとして使用
          const fallbackResponse = await caches.match(event.request);
          if (fallbackResponse) {
            logSecurityEvent('FALLBACK_TO_CACHE', { url: event.request.url });
            return fallbackResponse;
          }
          
          throw new Error(`Network response not ok: ${response.status}`);
        }
        
        // セキュアキャッシュ処理
        const secureResponse = await secureCache(event.request, response.clone());
        
        return secureResponse;
        
      } catch (error) {
        logSecurityEvent('FETCH_ERROR', { 
          url: event.request.url, 
          error: error.message 
        });
        
        // オフライン時のフォールバック
        if (event.request.destination === 'document') {
          const offlineResponse = await caches.match('/production-pwa.html');
          if (offlineResponse) {
            return offlineResponse;
          }
        }
        
        // エラーレスポンス
        return new Response('サービスが一時的に利用できません', {
          status: 503,
          statusText: 'Service Unavailable',
          headers: {
            'Content-Type': 'text/plain; charset=utf-8',
            'X-Content-Type-Options': 'nosniff',
            'X-Frame-Options': 'DENY'
          }
        });
      }
    })()
  );
});

// メッセージ処理（セキュア版）
self.addEventListener('message', event => {
  if (!event.data || typeof event.data.type !== 'string') {
    logSecurityEvent('INVALID_MESSAGE', { data: event.data });
    return;
  }
  
  const { type, payload = {} } = event.data;
  
  switch (type) {
    case 'SKIP_WAITING':
      logSecurityEvent('SKIP_WAITING_REQUESTED');
      self.skipWaiting();
      break;
      
    case 'GET_SECURITY_LOGS':
      logSecurityEvent('SECURITY_LOGS_REQUESTED');
      event.ports[0]?.postMessage({
        success: true,
        logs: securityLogs.slice(-20) // 最新20件のみ
      });
      break;
      
    case 'CLEAR_CACHE':
      logSecurityEvent('CACHE_CLEAR_REQUESTED');
      (async () => {
        try {
          await caches.delete(CACHE_NAME);
          event.ports[0]?.postMessage({ success: true });
        } catch (error) {
          logSecurityEvent('CACHE_CLEAR_FAILED', { error: error.message });
          event.ports[0]?.postMessage({ success: false, error: error.message });
        }
      })();
      break;
      
    case 'SECURITY_AUDIT':
      logSecurityEvent('SECURITY_AUDIT_REQUESTED');
      (async () => {
        try {
          const audit = await performSecurityAudit();
          event.ports[0]?.postMessage({ success: true, audit });
        } catch (error) {
          logSecurityEvent('SECURITY_AUDIT_FAILED', { error: error.message });
          event.ports[0]?.postMessage({ success: false, error: error.message });
        }
      })();
      break;
      
    default:
      logSecurityEvent('UNKNOWN_MESSAGE_TYPE', { type });
  }
});

// セキュリティ監査実行
async function performSecurityAudit() {
  const audit = {
    timestamp: new Date().toISOString(),
    cacheVersion: CACHE_NAME,
    cacheSize: 0,
    cachedUrls: [],
    securityViolations: securityLogs.filter(log => 
      log.event.includes('BLOCKED') || log.event.includes('ERROR')
    ).length,
    recentActivity: securityLogs.slice(-10)
  };
  
  try {
    const cache = await caches.open(CACHE_NAME);
    const requests = await cache.keys();
    audit.cachedUrls = requests.map(req => req.url);
    
    // キャッシュサイズ計算（概算）
    for (const request of requests) {
      const response = await cache.match(request);
      const blob = await response.blob();
      audit.cacheSize += blob.size;
    }
    
  } catch (error) {
    audit.error = error.message;
  }
  
  return audit;
}

// バックグラウンド同期
self.addEventListener('sync', event => {
  if (event.tag === 'security-audit') {
    logSecurityEvent('BACKGROUND_SECURITY_AUDIT');
    event.waitUntil(performSecurityAudit());
  }
});

// エラーハンドリング
self.addEventListener('error', event => {
  logSecurityEvent('SERVICE_WORKER_ERROR', {
    message: event.message,
    filename: event.filename,
    lineno: event.lineno,
    colno: event.colno
  });
});

self.addEventListener('unhandledrejection', event => {
  logSecurityEvent('UNHANDLED_PROMISE_REJECTION', {
    reason: event.reason?.message || String(event.reason)
  });
});

// 定期的なセキュリティチェック
setInterval(() => {
  cleanupExpiredCache();
  
  // セキュリティログのローテーション
  if (securityLogs.length > SECURITY_CONFIG.MAX_LOG_ENTRIES) {
    securityLogs.splice(0, securityLogs.length - SECURITY_CONFIG.MAX_LOG_ENTRIES);
    logSecurityEvent('SECURITY_LOG_ROTATED');
  }
}, 60000); // 1分ごと

logSecurityEvent('SECURE_SERVICE_WORKER_LOADED', { 
  version: CACHE_NAME,
  config: SECURITY_CONFIG 
});