// Service Worker for YouTube→NotebookLM PWA
const CACHE_NAME = 'yt-notebooklm-v1.0.0';
const CACHE_URLS = [
  '/',
  '/pwa-main.html',
  '/manifest.json'
];

// インストール時のキャッシュ設定
self.addEventListener('install', event => {
  console.log('Service Worker: Installing...');
  
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Service Worker: Caching files');
        return cache.addAll(CACHE_URLS);
      })
      .then(() => {
        console.log('Service Worker: Cache completed');
        return self.skipWaiting();
      })
      .catch(error => {
        console.error('Service Worker: Cache failed', error);
      })
  );
});

// アクティベーション時の古いキャッシュ削除
self.addEventListener('activate', event => {
  console.log('Service Worker: Activating...');
  
  event.waitUntil(
    caches.keys()
      .then(cacheNames => {
        return Promise.all(
          cacheNames.map(cache => {
            if (cache !== CACHE_NAME) {
              console.log('Service Worker: Deleting old cache', cache);
              return caches.delete(cache);
            }
          })
        );
      })
      .then(() => {
        console.log('Service Worker: Activation completed');
        return self.clients.claim();
      })
  );
});

// フェッチイベントの処理（キャッシュファースト戦略）
self.addEventListener('fetch', event => {
  // GET リクエストのみ処理
  if (event.request.method !== 'GET') {
    return;
  }
  
  // NotebookLMや外部サイトへのリクエストは直接通す
  const url = new URL(event.request.url);
  if (url.hostname !== self.location.hostname) {
    return;
  }
  
  event.respondWith(
    caches.match(event.request)
      .then(cachedResponse => {
        // キャッシュがある場合はそれを返す
        if (cachedResponse) {
          console.log('Service Worker: Serving from cache', event.request.url);
          return cachedResponse;
        }
        
        // キャッシュがない場合はネットワークから取得
        console.log('Service Worker: Fetching from network', event.request.url);
        return fetch(event.request)
          .then(response => {
            // レスポンスが有効な場合はキャッシュに追加
            if (response.status === 200) {
              const responseClone = response.clone();
              caches.open(CACHE_NAME)
                .then(cache => {
                  cache.put(event.request, responseClone);
                });
            }
            return response;
          })
          .catch(error => {
            console.error('Service Worker: Fetch failed', error);
            
            // オフライン時のフォールバック
            if (event.request.destination === 'document') {
              return caches.match('/pwa-main.html');
            }
            
            // その他のリソースのエラー
            return new Response('オフラインです', {
              status: 503,
              statusText: 'Service Unavailable',
              headers: new Headers({
                'Content-Type': 'text/plain; charset=utf-8'
              })
            });
          });
      })
  );
});

// メッセージ処理（アプリからの指示を受信）
self.addEventListener('message', event => {
  if (event.data && event.data.type) {
    switch (event.data.type) {
      case 'SKIP_WAITING':
        console.log('Service Worker: Skip waiting requested');
        self.skipWaiting();
        break;
        
      case 'CACHE_URLS':
        console.log('Service Worker: Additional cache requested');
        const urls = event.data.urls || [];
        caches.open(CACHE_NAME)
          .then(cache => cache.addAll(urls))
          .then(() => {
            event.ports[0].postMessage({ success: true });
          })
          .catch(error => {
            console.error('Service Worker: Additional cache failed', error);
            event.ports[0].postMessage({ success: false, error: error.message });
          });
        break;
        
      case 'CLEAR_CACHE':
        console.log('Service Worker: Cache clear requested');
        caches.delete(CACHE_NAME)
          .then(() => {
            event.ports[0].postMessage({ success: true });
          })
          .catch(error => {
            console.error('Service Worker: Cache clear failed', error);
            event.ports[0].postMessage({ success: false, error: error.message });
          });
        break;
        
      default:
        console.log('Service Worker: Unknown message type', event.data.type);
    }
  }
});

// バックグラウンド同期（将来の機能拡張用）
self.addEventListener('sync', event => {
  if (event.tag === 'background-sync') {
    console.log('Service Worker: Background sync triggered');
    // 将来的に使用履歴の同期などに使用可能
  }
});

// プッシュ通知（将来の機能拡張用）
self.addEventListener('push', event => {
  if (event.data) {
    const data = event.data.json();
    console.log('Service Worker: Push notification received', data);
    
    const options = {
      body: data.body || 'YouTube→NotebookLMからの通知',
      icon: '/icon-192.png',
      badge: '/icon-192.png',
      tag: 'yt-notebooklm',
      renotify: true,
      actions: [
        {
          action: 'open',
          title: '開く'
        },
        {
          action: 'close',
          title: '閉じる'
        }
      ]
    };
    
    event.waitUntil(
      self.registration.showNotification(data.title || 'YouTube→NotebookLM', options)
    );
  }
});

// 通知クリック処理
self.addEventListener('notificationclick', event => {
  console.log('Service Worker: Notification clicked', event.action);
  
  event.notification.close();
  
  if (event.action === 'open' || !event.action) {
    event.waitUntil(
      clients.openWindow('/')
    );
  }
});

console.log('Service Worker: Registered successfully');