package org.dubini.backofficeAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dubini.backofficeAPI.config.SupabaseStorageProperties;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class SliderImageService {

    private final WebClient webClient;
    private final SupabaseStorageProperties supabaseStorageProperties;
    private final ObjectMapper objectMapper;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final String SLIDER_DIRECTORY = "slider";
    private static final Set<String> ALLOWED_SLIDE_NAMES = Set.of("slide1", "slide2", "slide3", "slide4", "slide5", "slide6");

    public SliderImageService(WebClient.Builder webClientBuilder, SupabaseStorageProperties supabaseStorageProperties) {
        this.supabaseStorageProperties = supabaseStorageProperties;
        this.objectMapper = new ObjectMapper();

        String baseUrl = supabaseStorageProperties.getApi();
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            baseUrl = "https://" + baseUrl;
        }

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + supabaseStorageProperties.getKey())
                .defaultHeader("apikey", supabaseStorageProperties.getKey())
                .build();

        log.info("SliderImageService configurado con Supabase URL: {}", baseUrl);
    }

    /**
     * Actualiza una imagen de slide (slide1 a slide6)
     *
     * @param slideName El nombre del slide (slide1, slide2, ..., slide6)
     * @param file La nueva imagen
     * @return ImageResponseDTO con la información de la imagen actualizada
     * @throws IOException Si hay error en la validación o subida
     */
    public ImageResponseDTO updateSliderImage(String slideName, MultipartFile file) throws IOException {
        log.debug("Actualizando imagen slider: {}", slideName);

        // Validar que sea uno de los nombres permitidos
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IOException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        // Validar la imagen
        validateImage(file);

        byte[] imageBytes = file.getBytes();
        String fileName = slideName + ".webp";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/webp";

        try {
            // Ruta en Supabase: /slider/slide1.webp (o slide2, ..., slide6)
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    fileName);

            log.debug("Subiendo imagen slider a ruta: {}", storagePath);

            webClient.put()
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

            String imageUrl = "%s/storage/v1/object/public/%s/%s/%s".formatted(
                    supabaseStorageProperties.getApi(),
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    fileName);

            log.info("Imagen slider actualizada correctamente: {}", imageUrl);

            return new ImageResponseDTO(fileName, imageUrl, imageBytes.length);

        } catch (Exception e) {
            log.error("Falló la actualización de imagen slider en Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al actualizar la imagen slider en Supabase Storage", e);
        }
    }

    /**
     * Actualiza el caption (pie de texto) de un slide
     *
     * @param slideName El nombre del slide (slide1, ..., slide6)
     * @param caption El texto del caption
     * @throws IOException Si hay error al guardar
     */
    public void updateSliderCaption(String slideName, String caption) throws IOException {
        log.debug("Actualizando caption del slide: {}", slideName);

        // Validar que sea uno de los nombres permitidos
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IOException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        // Validar caption
        if (caption == null || caption.trim().isEmpty()) {
            throw new IOException("El caption no puede estar vacío");
        }

        if (caption.length() > 150) {
            throw new IOException("El caption no puede exceder 150 caracteres");
        }

        try {
            // Crear objeto con solo el caption
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("caption", caption.trim());

            byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);

            // Ruta para guardar metadatos: /slider/slide1.json
            String storagePath = "/storage/v1/object/%s/%s/%s.json".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    slideName);

            log.debug("Guardando caption en ruta: {}", storagePath);

            webClient.put()
                    .uri(storagePath)
                    .header("Content-Type", "application/json")
                    .bodyValue(metadataBytes)
                    .retrieve()
                    .toBodilessEntity()
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.error("Supabase error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Error al contactar con Supabase Storage"));
                    })
                    .block();

            log.info("Caption del slide actualizado correctamente: {}", slideName);

        } catch (Exception e) {
            log.error("Falló la actualización del caption en Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al actualizar el caption en Supabase Storage", e);
        }
    }

    /**
     * Obtiene el caption de un slide
     *
     * @param slideName El nombre del slide
     * @return El caption del slide
     * @throws IOException Si hay error al obtener
     */
    public String getSliderCaption(String slideName) throws IOException {
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IOException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s.json".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    slideName);

            String response = webClient.get()
                    .uri(storagePath)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.warn("Caption no encontrado para slide {}: HTTP {}", slideName, e.getStatusCode());
                        return Mono.just("{}");
                    })
                    .block();

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = objectMapper.readValue(response, Map.class);
            return (String) metadata.getOrDefault("caption", "");

        } catch (Exception e) {
            log.warn("Error al obtener caption del slide {}: {}", slideName, e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene la URL pública de una imagen slide
     *
     * @param slideName El nombre del slide
     * @return La URL pública de la imagen
     * @throws IOException Si el nombre no es válido
     */
    public String getSliderImageUrl(String slideName) throws IOException {
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IOException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        String fileName = slideName + ".webp";
        return "%s/storage/v1/object/public/%s/%s/%s".formatted(
                supabaseStorageProperties.getApi(),
                supabaseStorageProperties.getBucket(),
                SLIDER_DIRECTORY,
                fileName);
    }

    /**
     * Elimina una imagen slide de Supabase
     *
     * @param slideName El nombre del slide
     * @throws IOException Si hay error al eliminar o nombre no válido
     */
    public void deleteSliderImage(String slideName) throws IOException {
        if (!ALLOWED_SLIDE_NAMES.contains(slideName)) {
            throw new IOException("Nombre de slide no válido. Permitidos: slide1 a slide6");
        }

        String fileName = slideName + ".webp";

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    SLIDER_DIRECTORY,
                    fileName);

            webClient.delete()
                    .uri(storagePath)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Imagen slide eliminada correctamente: {}", fileName);

        } catch (Exception e) {
            log.error("Error al eliminar imagen slide de Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al eliminar la imagen slide de Supabase Storage", e);
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
}
