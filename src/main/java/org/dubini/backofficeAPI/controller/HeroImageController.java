package org.dubini.backofficeAPI.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.dubini.backofficeAPI.service.HeroImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/hero-images")
@RequiredArgsConstructor
public class HeroImageController {

    private final HeroImageService heroImageService;

    /**
     * Actualiza una imagen hero específica (hero1, hero2 o hero3)
     *
     * @param heroName El nombre de la imagen: hero1, hero2 o hero3
     * @param file La nueva imagen en formato JPEG, PNG, WEBP o GIF
     * @return ResponseEntity con la información de la imagen actualizada
     */
    @PutMapping(value = "/{heroName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> updateHeroImage(
            @PathVariable String heroName,
            @RequestParam("image") MultipartFile file) {

        try {
            log.info("Solicitud para actualizar imagen hero: {}", heroName);

            if (file.isEmpty()) {
                log.warn("Intento de actualización con archivo vacío");
                return ResponseEntity.badRequest().build();
            }

            ImageResponseDTO response = heroImageService.updateHeroImage(heroName, file);
            log.info("Imagen hero {} actualizada exitosamente", heroName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validación fallida para imagen hero {}: {}", heroName, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error al actualizar imagen hero {}: {}", heroName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene la URL pública de una imagen hero
     *
     * @param heroName El nombre de la imagen: hero1, hero2 o hero3
     * @return ResponseEntity con la URL de la imagen
     */
    @GetMapping(value = "/{heroName}/url")
    public ResponseEntity<HeroImageUrlResponse> getHeroImageUrl(
            @PathVariable String heroName) {

        try {
            log.debug("Solicitud para obtener URL de imagen hero: {}", heroName);
            String imageUrl = heroImageService.getHeroImageUrl(heroName);
            return ResponseEntity.ok(new HeroImageUrlResponse(heroName, imageUrl));

        } catch (IOException e) {
            log.warn("Imagen hero no válida: {}", heroName);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina una imagen hero específica (la reemplaza con una imagen vacía o la elimina)
     *
     * @param heroName El nombre de la imagen: hero1, hero2 o hero3
     * @return ResponseEntity sin contenido si es exitoso
     */
    @DeleteMapping("/{heroName}")
    public ResponseEntity<Void> deleteHeroImage(
            @PathVariable String heroName) {

        try {
            log.info("Solicitud para eliminar imagen hero: {}", heroName);
            heroImageService.deleteHeroImage(heroName);
            log.info("Imagen hero {} eliminada exitosamente", heroName);
            return ResponseEntity.noContent().build();

        } catch (IOException e) {
            log.warn("Error al eliminar imagen hero {}: {}", heroName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DTO interno para responder con la URL de la imagen hero
     */
    public static class HeroImageUrlResponse {
        public String heroName;
        public String url;

        public HeroImageUrlResponse(String heroName, String url) {
            this.heroName = heroName;
            this.url = url;
        }

        public String getHeroName() {
            return heroName;
        }

        public void setHeroName(String heroName) {
            this.heroName = heroName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
