const VERSION = "v10.0.2";
const CACHE_NAME = `dubini-static-cache-${VERSION}`;

const SHELL_KEY = "Application loading";

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(["/"])));
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) =>
      Promise.all(
        cacheNames.map((cacheName) => {
          if (
            cacheName.startsWith("dubini-static-cache-") &&
            cacheName !== CACHE_NAME
          ) {
            return caches.delete(cacheName);
          }
        })
      )
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url); 

    if (
    event.request.destination === "image" &&
    url.href.startsWith(
      "https://mcybqxqlujczgclidnar.supabase.co/storage/v1/object/public/ajpd-storage/"
    )
  ) {
    event.respondWith(fetch(event.request));
    return;
  }

  if (event.request.mode === "navigate") {
    event.respondWith(
      caches.match(event.request).then((cached) => {
        if (cached) return cached; 

        return fetch(event.request)
          .then((networkResponse) => {
            const responseToInspect = networkResponse.clone();

            return responseToInspect.text().then((text) => {
              // Inspección: Verificamos si la respuesta HTML contiene la clave del SHELL.
              const isShellContent = text.includes(SHELL_KEY);

              return caches.open(CACHE_NAME).then((cache) => {
                if (!isShellContent) {
                  cache.put(event.request, networkResponse.clone());
                }

                return networkResponse;
              });
            });
          })
          .catch(() => caches.match("/"));
      })
    );
    return;
  } //
  if (
    event.request.destination === "script" ||
    event.request.destination === "style" ||
    event.request.destination === "image" ||
    event.request.destination === "font"
  ) {
    event.respondWith(
      caches.match(event.request).then((cached) => {
        if (cached) return cached;

        return fetch(event.request).then((networkResponse) =>
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, networkResponse.clone());
            return networkResponse;
          })
        );
      })
    );
  }
});
