package org.dubini.backofficeAPI.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para servir el manifest.json con el MIME type correcto
 * Necesario para que la PWA (Progressive Web App) sea instalable en navegadores
 */
@RestController
public class ManifestController {

    /**
     * Endpoint para servir manifest.json con el MIME type application/json
     * Las PWAs requieren que el manifest se sirva con el tipo MIME correcto
     * para que los navegadores reconozcan la aplicación como instalable
     */
    @GetMapping("/manifest.json")
    public ResponseEntity<String> getManifest() {
        String manifest = """
                {
                  "name": "Proyecto Dubini Backoffice",
                  "short_name": "Dubini Backoffice",
                  "description": "Backoffice de la Asociación Juvenil Proyecto Dubini",
                  "start_url": "/login",
                  "scope": "/",
                  "display": "standalone",
                  "orientation": "portrait-primary",
                  "theme_color": "#1e88e5",
                  "background_color": "#ffffff",
                  "prefer_related_applications": false,
                  "categories": ["business", "productivity"],
                  "icons": [
                    {
                      "src": "/assets/logo.png",
                      "sizes": "192x192",
                      "type": "image/png",
                      "purpose": "any"
                    },
                    {
                      "src": "/assets/logo.png",
                      "sizes": "512x512",
                      "type": "image/png",
                      "purpose": "any"
                    },
                    {
                      "src": "/assets/logo.png",
                      "sizes": "192x192",
                      "type": "image/png",
                      "purpose": "maskable"
                    },
                    {
                      "src": "/assets/logo.png",
                      "sizes": "512x512",
                      "type": "image/png",
                      "purpose": "maskable"
                    }
                  ]
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(ContentDisposition.inline().filename("manifest.json").build());
        headers.setCacheControl("public, max-age=3600");

        return ResponseEntity.ok().headers(headers).body(manifest);
    }
}
