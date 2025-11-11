package org.dubini.backofficeAPI.controller;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.dubini.backofficeAPI.dto.response.EditorJSImageResponseDTO;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.dubini.backofficeAPI.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    @SuppressWarnings("unused")
    private static final int DEFAULT_WIDTH = 800;
    @SuppressWarnings("unused")
    private static final int DEFAULT_HEIGHT = 600;
    @SuppressWarnings("unused")
    private static final float DEFAULT_QUALITY = 0.8f;
    private static final int MAX_WIDTH = 4096;
    private static final int MAX_HEIGHT = 4096;
    private static final int MIN_DIMENSION = 50;

    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EditorJSImageResponseDTO> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "width", defaultValue = "800") @Min(value = MIN_DIMENSION, message = "El ancho mínimo es 50px") @Max(value = MAX_WIDTH, message = "El ancho máximo es 4096px") int width,
            @RequestParam(value = "height", defaultValue = "600") @Min(value = MIN_DIMENSION, message = "La altura mínima es 50px") @Max(value = MAX_HEIGHT, message = "La altura máxima es 4096px") int height,
            @RequestParam(value = "quality", defaultValue = "0.8") @DecimalMin(value = "0.1", message = "La calidad mínima es 0.1") @DecimalMax(value = "1.0", message = "La calidad máxima es 1.0") float quality)
            throws IOException {

        log.debug("Upload request received - file: {}, width: {}, height: {}, quality: {}",
                file.getOriginalFilename(), width, height, quality);

        validateFile(file);

        ImageResponseDTO response = imageService.saveImage(file, width, height, quality);

        log.info("Image uploaded successfully: {}", response.getFileName());

        return ResponseEntity.ok(
                EditorJSImageResponseDTO.success(
                        response.getUrl(),
                        response.getFileName(),
                        response.getSize()));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            log.warn("Upload attempt with empty file");
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Upload attempt with invalid content type: {}", contentType);
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.warn("Upload attempt with file too large: {} bytes", file.getSize());
            throw new IllegalArgumentException("El archivo no debe superar 10MB");
        }
    }
}