// Simple Service Worker for Improved PWA
'use strict';

const CACHE_NAME = 'yt-notebooklm-improved-v1.0.0';
const CACHE_URLS = [
  './improved-pwa.html',
  './improved-manifest.json'
];

// Install event
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

// Activate event
self.addEventListener('activate', event => {
  console.log('Service Worker: Activating...');
  
  event.waitUntil(
    caches.keys()
      .then(cacheNames => {
        return Promise.all(
          cacheNames.map(cache => {
            if (cache !== CACHE_NAME && cache.startsWith('yt-notebooklm-')) {
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

// Fetch event - simple cache first strategy
self.addEventListener('fetch', event => {
  // Only handle GET requests to our domain
  if (event.request.method !== 'GET') {
    return;
  }
  
  const url = new URL(event.request.url);
  if (url.hostname !== self.location.hostname) {
    return;
  }
  
  event.respondWith(
    caches.match(event.request)
      .then(cachedResponse => {
        if (cachedResponse) {
          console.log('Service Worker: Serving from cache', event.request.url);
          return cachedResponse;
        }
        
        console.log('Service Worker: Fetching from network', event.request.url);
        return fetch(event.request)
          .then(response => {
            // Cache successful responses
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
            
            // Fallback for navigation requests
            if (event.request.destination === 'document') {
              return caches.match('./improved-pwa.html');
            }
            
            return new Response('オフラインです', {
              status: 503,
              statusText: 'Service Unavailable'
            });
          });
      })
  );
});

// Message handling
self.addEventListener('message', event => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});

console.log('Simple Service Worker loaded successfully');