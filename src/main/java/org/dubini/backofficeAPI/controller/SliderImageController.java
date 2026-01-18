package org.dubini.backofficeAPI.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.dubini.backofficeAPI.service.SliderImageService;
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
@RequestMapping("/api/slider-images")
@RequiredArgsConstructor
public class SliderImageController {

    private final SliderImageService sliderImageService;

    /**
     * Actualiza una imagen de slide específica (slide1 a slide6)
     *
     * @param slideName El nombre del slide: slide1, slide2, ..., slide6
     * @param file La nueva imagen en formato JPEG, PNG, WEBP o GIF
     * @return ResponseEntity con la información de la imagen actualizada
     */
    @PutMapping(value = "/{slideName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> updateSliderImage(
            @PathVariable String slideName,
            @RequestParam("image") MultipartFile file) {

        try {
            log.info("Solicitud para actualizar imagen slider: {}", slideName);

            if (file.isEmpty()) {
                log.warn("Intento de actualización con archivo vacío");
                return ResponseEntity.badRequest().build();
            }

            ImageResponseDTO response = sliderImageService.updateSliderImage(slideName, file);
            log.info("Imagen slider {} actualizada exitosamente", slideName);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validación fallida para imagen slider {}: {}", slideName, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error al actualizar imagen slider {}: {}", slideName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza el caption (pie de texto) de un slide
     *
     * @param slideName El nombre del slide: slide1, ..., slide6
     * @param request Objeto con el caption
     * @return ResponseEntity con mensaje de éxito
     */
    @PutMapping("/{slideName}/caption")
    public ResponseEntity<SliderCaptionUpdateResponse> updateSliderCaption(
            @PathVariable String slideName,
            @RequestBody SliderCaptionRequest request) {

        try {
            log.info("Solicitud para actualizar caption de slide: {}", slideName);

            if (request.getCaption() == null || request.getCaption().trim().isEmpty()) {
                log.warn("Caption vacío para slide {}", slideName);
                return ResponseEntity.badRequest().build();
            }

            sliderImageService.updateSliderCaption(slideName, request.getCaption());
            log.info("Caption del slide {} actualizado exitosamente", slideName);

            return ResponseEntity.ok(new SliderCaptionUpdateResponse(slideName, "Caption actualizado exitosamente"));

        } catch (IOException e) {
            log.warn("Error al actualizar caption de slide {}: {}", slideName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene la información de un slide (URL de imagen y caption)
     *
     * @param slideName El nombre del slide: slide1, ..., slide6
     * @return ResponseEntity con la información del slide
     */
    @GetMapping("/{slideName}")
    public ResponseEntity<SliderInfoResponse> getSliderInfo(
            @PathVariable String slideName) {

        try {
            log.debug("Solicitud para obtener información de slide: {}", slideName);
            String imageUrl = sliderImageService.getSliderImageUrl(slideName);
            String caption = sliderImageService.getSliderCaption(slideName);

            return ResponseEntity.ok(new SliderInfoResponse(slideName, imageUrl, caption));

        } catch (IOException e) {
            log.warn("Slide no válido: {}", slideName);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene solo la URL de la imagen de un slide
     *
     * @param slideName El nombre del slide
     * @return ResponseEntity con la URL de la imagen
     */
    @GetMapping(value = "/{slideName}/url")
    public ResponseEntity<SliderImageUrlResponse> getSliderImageUrl(
            @PathVariable String slideName) {

        try {
            log.debug("Solicitud para obtener URL de imagen slider: {}", slideName);
            String imageUrl = sliderImageService.getSliderImageUrl(slideName);
            return ResponseEntity.ok(new SliderImageUrlResponse(slideName, imageUrl));

        } catch (IOException e) {
            log.warn("Slide no válido: {}", slideName);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene solo el caption de un slide
     *
     * @param slideName El nombre del slide
     * @return ResponseEntity con el caption
     */
    @GetMapping("/{slideName}/caption")
    public ResponseEntity<SliderCaptionResponse> getSliderCaption(
            @PathVariable String slideName) {

        try {
            log.debug("Solicitud para obtener caption de slide: {}", slideName);
            String caption = sliderImageService.getSliderCaption(slideName);
            return ResponseEntity.ok(new SliderCaptionResponse(slideName, caption));

        } catch (IOException e) {
            log.warn("Slide no válido: {}", slideName);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Elimina una imagen de slide específica
     *
     * @param slideName El nombre del slide: slide1, ..., slide6
     * @return ResponseEntity sin contenido si es exitoso
     */
    @DeleteMapping("/{slideName}")
    public ResponseEntity<Void> deleteSliderImage(
            @PathVariable String slideName) {

        try {
            log.info("Solicitud para eliminar imagen slider: {}", slideName);
            sliderImageService.deleteSliderImage(slideName);
            log.info("Imagen slider {} eliminada exitosamente", slideName);
            return ResponseEntity.noContent().build();

        } catch (IOException e) {
            log.warn("Error al eliminar imagen slider {}: {}", slideName, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============ DTOs ============

    public static class SliderCaptionRequest {
        private String caption;

        public SliderCaptionRequest() {
        }

        public SliderCaptionRequest(String caption) {
            this.caption = caption;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public static class SliderInfoResponse {
        public String slideName;
        public String imageUrl;
        public String caption;

        public SliderInfoResponse(String slideName, String imageUrl, String caption) {
            this.slideName = slideName;
            this.imageUrl = imageUrl;
            this.caption = caption;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public static class SliderImageUrlResponse {
        public String slideName;
        public String url;

        public SliderImageUrlResponse(String slideName, String url) {
            this.slideName = slideName;
            this.url = url;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class SliderCaptionResponse {
        public String slideName;
        public String caption;

        public SliderCaptionResponse(String slideName, String caption) {
            this.slideName = slideName;
            this.caption = caption;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public static class SliderCaptionUpdateResponse {
        public String slideName;
        public String message;

        public SliderCaptionUpdateResponse(String slideName, String message) {
            this.slideName = slideName;
            this.message = message;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
