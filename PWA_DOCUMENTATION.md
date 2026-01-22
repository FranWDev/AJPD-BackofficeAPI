# PWA (Progressive Web App) - Documentación

## ¿Qué es una PWA?

Una Progressive Web App (PWA) es una aplicación web que puede ser instalada como una app nativa en dispositivos móviles y de escritorio. Proporciona una experiencia similar a la de una app nativa con las ventajas de la web.

## Configuración realizada en Proyecto Dubini

### 1. Manifest.json
Archivo: `/src/main/java/org/dubini/backofficeAPI/controller/ManifestController.java`

El manifest se sirve dinámicamente a través de un controller REST que garantiza que el MIME type sea `application/json`. Esto es **crítico** para que los navegadores reconozcan la PWA como instalable.
- **Nombre de la app**: "Proyecto Dubini Backoffice"
- **Nombre corto**: "Dubini"
- **Página de inicio**: `/editor`
- **Modo de visualización**: `standalone` (pantalla completa sin barra de navegador)
- **Tema de color**: `#1e88e5` (azul)
- **Iconos**: Configurados en varios tamaños (192x192, 512x512) con soporte para maskable icons
- **Shortcuts**: Acceso directo al editor

### 2. Service Worker
Archivo: `/src/main/resources/static/sw.js`

El Service Worker ya estaba implementado en el proyecto y maneja:
- Caching de recursos estáticos
- Gestión de versiones de caché
- Actualización inteligente de caché
- Offline support (parcial)

### 3. Meta tags PWA
Se han añadido los siguientes meta tags en todos los HTML principales (login.html, editor.html, news.html):

```html
<!-- Tema de color -->
<meta name="theme-color" content="#1e88e5">

<!-- iOS Support -->
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
<meta name="apple-mobile-web-app-title" content="Dubini">

<!-- iOS Icon -->
<link rel="apple-touch-icon" href="/assets/logo.png">

<!-- Favicon -->
<link rel="icon" type="image/x-icon" href="/favicon.ico">

<!-- Manifest -->
<link rel="manifest" href="/manifest.json">
```

### 4. Service Worker Registration
Se han añadido scripts en todos los HTML para registrar el Service Worker:

```javascript
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').then(registration => {
        console.log('Service Worker registrado:', registration);
    }).catch(error => {
        console.error('Error registrando Service Worker:', error);
    });
}
```

## Cómo instalar la app

### En dispositivos Android:
1. Abre el backoffice en Chrome/Firefox
2. Presiona el menú (⋮)
3. Selecciona "Instalar aplicación" o "Añadir a pantalla de inicio"
4. La app se instalará como una aplicación nativa

### En dispositivos iOS/iPadOS:
1. Abre el backoffice en Safari
2. Presiona el botón de compartir
3. Selecciona "Añadir a pantalla de inicio"
4. La app aparecerá en tu pantalla de inicio

### En navegadores de escritorio:
Algunos navegadores (Chrome, Edge) también ofrecen la opción de instalar como app.

## Características de la PWA instalada

- ✅ Acceso offline (recursos en caché)
- ✅ Acceso rápido desde pantalla de inicio
- ✅ Experiencia inmersiva (sin barra de navegador)
- ✅ Iconos personalizados
- ✅ Tema de color consistente
- ✅ Service Worker para mejor rendimiento

## Archivos modificados/creados

1. `/src/main/java/org/dubini/backofficeAPI/controller/ManifestController.java` - Nuevo controller que sirve manifest.json con MIME type correcto
2. `/src/main/java/org/dubini/backofficeAPI/config/SecurityConfig.java` - Permitir acceso público a `/manifest.json` y `/sw.js`
3. `/src/main/java/org/dubini/backofficeAPI/config/NativeHintsConfig.java` - Registrar ManifestController para GraalVM
4. `/src/main/resources/templates/login.html` - Meta tags y SW registration añadidos
5. `/src/main/resources/templates/editor.html` - Meta tags y SW registration añadidos
6. `/src/main/resources/templates/news.html` - Meta tags y SW registration añadidos
7. `/src/main/resources/static/sw.js` - Ya existente (sin cambios)

## Verificación

Para verificar que todo está configurado correctamente:

1. Compila y ejecuta la aplicación
2. Abre el backoffice en un navegador móvil (Chrome/Firefox en Android)
3. En Chrome Android, presiona el menú (⋮) y busca "Instalar aplicación" o similar
4. Si la instalación no aparece, verifica en DevTools:
   - Abre DevTools (F12)
   - Ve a "Application" → "Manifest"
   - Verifica que el MIME type sea `application/json` (puedes ver en el Network tab)
   - Busca errores de validación del manifest
5. En "Service Workers" verifica que esté registrado correctamente

## Notas importantes

- El manifest.json se sirve **dinámicamente** desde `ManifestController.java` con el MIME type correcto (`application/json`)
- Esto es diferente a servir un archivo estático, que podría no tener el MIME type correcto
- Spring Security permite acceso público a `/manifest.json` y `/sw.js`
- La PWA funciona mejor con HTTPS en producción (aunque localhost funciona en desarrollo)
- Los iconos en el manifest apuntan a `/assets/logo.png` (verifica que exista este archivo)
- El Service Worker ya estaba implementado en el proyecto
