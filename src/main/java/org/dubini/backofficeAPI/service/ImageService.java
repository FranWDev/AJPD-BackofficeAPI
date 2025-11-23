package org.dubini.backofficeAPI.service;

import lombok.extern.slf4j.Slf4j;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.dubini.backofficeAPI.config.SupabaseStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;
import java.util.Set;

@Slf4j
@Service
public class ImageService {

    private final WebClient webClient;
    private final SupabaseStorageProperties supabaseStorageProperties;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ImageService(WebClient.Builder webClientBuilder, SupabaseStorageProperties supabaseStorageProperties) {
        this.supabaseStorageProperties = supabaseStorageProperties;

        String baseUrl = supabaseStorageProperties.getApi();
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + supabaseStorageProperties.getKey())
                .defaultHeader("apikey", supabaseStorageProperties.getKey())
                .build();

        log.info("ImageService configurado con Supabase URL: {}", baseUrl);
    }

    public ImageResponseDTO saveImage(MultipartFile file) throws IOException {
        log.debug("Subiendo imagen a Supabase: {}", file.getOriginalFilename());

        // Validaciones básicas
        validateImage(file);

        byte[] imageBytes = file.getBytes();
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        try {
            String storagePath = "/storage/v1/object/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    fileName);

            webClient.post()
                    .uri(storagePath)
                    .header("Content-Type", contentType)
                    .bodyValue(imageBytes)
                    .retrieve()
                    .toBodilessEntity()
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.error("Supabase error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Error al contactar con Supabase Storage"));
                    })
                    .block();

            String imageUrl = "%s/storage/v1/object/public/%s/%s".formatted(
                    supabaseStorageProperties.getApi(),
                    supabaseStorageProperties.getBucket(),
                    fileName);

            log.info("Imagen subida correctamente a Supabase: {}", imageUrl);

            return new ImageResponseDTO(fileName, imageUrl, imageBytes.length);

        } catch (Exception e) {
            log.error("Falló la subida a Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al subir la imagen a Supabase Storage", e);
        }
    }

    private void validateImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("Tipo de archivo no permitido. Solo se aceptan imágenes JPEG, PNG, WEBP y GIF");
        }

        // Validación adicional por extensión
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!Set.of("jpg", "jpeg", "png", "webp", "gif").contains(extension)) {
                throw new IOException("Extensión de archivo no válida");
            }
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = getFileExtension(originalFilename);

        // Si no hay extensión o no es válida, usar jpg por defecto
        if (extension.isEmpty() || !Set.of("jpg", "jpeg", "png", "webp", "gif").contains(extension.toLowerCase())) {
            extension = "jpg";
        }

        return "%s_%s.%s".formatted(timestamp, uuid, extension);
    }

    public void deleteImage(String fileName) throws IOException {
        try {
            String storagePath = "/storage/v1/object/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    fileName);

            webClient.delete()
                    .uri(storagePath)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Imagen eliminada correctamente: {}", fileName);

        } catch (Exception e) {
            log.error("Error al eliminar imagen de Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al eliminar la imagen de Supabase Storage", e);
        }
    }
}