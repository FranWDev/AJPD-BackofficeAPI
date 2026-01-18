package org.dubini.backofficeAPI.service;

import lombok.extern.slf4j.Slf4j;
import org.dubini.backofficeAPI.config.SupabaseStorageProperties;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Service
public class HeroImageService {

    private final WebClient webClient;
    private final SupabaseStorageProperties supabaseStorageProperties;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final String HERO_DIRECTORY = "hero";
    private static final Set<String> ALLOWED_HERO_NAMES = Set.of("hero1", "hero2", "hero3");

    public HeroImageService(WebClient.Builder webClientBuilder, SupabaseStorageProperties supabaseStorageProperties) {
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

        log.info("HeroImageService configurado con Supabase URL: {}", baseUrl);
    }

    /**
     * Actualiza una imagen hero (hero1, hero2 o hero3)
     * Reemplaza la imagen existente con la nueva
     *
     * @param heroName El nombre de la imagen (hero1, hero2, hero3)
     * @param file La nueva imagen
     * @return ImageResponseDTO con la información de la imagen actualizada
     * @throws IOException Si hay error en la validación o subida
     */
    public ImageResponseDTO updateHeroImage(String heroName, MultipartFile file) throws IOException {
        log.debug("Actualizando imagen hero: {}", heroName);

        // Validar que sea uno de los nombres permitidos
        if (!ALLOWED_HERO_NAMES.contains(heroName)) {
            throw new IOException("Nombre de imagen hero no válido. Permitidos: hero1, hero2, hero3");
        }

        // Validar la imagen
        validateImage(file);

        byte[] imageBytes = file.getBytes();
        String fileName = heroName + ".webp";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/webp";

        try {
            // Ruta en Supabase: /hero/hero1.webp (o hero2, hero3)
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    HERO_DIRECTORY,
                    fileName);

            log.debug("Subiendo imagen a ruta: {}", storagePath);

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
                    HERO_DIRECTORY,
                    fileName);

            log.info("Imagen hero actualizada correctamente: {}", imageUrl);

            return new ImageResponseDTO(fileName, imageUrl, imageBytes.length);

        } catch (Exception e) {
            log.error("Falló la actualización de imagen hero en Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al actualizar la imagen hero en Supabase Storage", e);
        }
    }

    /**
     * Obtiene la URL pública de una imagen hero
     *
     * @param heroName El nombre de la imagen (hero1, hero2, hero3)
     * @return La URL pública de la imagen
     * @throws IOException Si el nombre no es válido
     */
    public String getHeroImageUrl(String heroName) throws IOException {
        if (!ALLOWED_HERO_NAMES.contains(heroName)) {
            throw new IOException("Nombre de imagen hero no válido. Permitidos: hero1, hero2, hero3");
        }

        String fileName = heroName + ".webp";
        return "%s/storage/v1/object/public/%s/%s/%s".formatted(
                supabaseStorageProperties.getApi(),
                supabaseStorageProperties.getBucket(),
                HERO_DIRECTORY,
                fileName);
    }

    /**
     * Elimina una imagen hero de Supabase
     *
     * @param heroName El nombre de la imagen (hero1, hero2, hero3)
     * @throws IOException Si hay error al eliminar o nombre no válido
     */
    public void deleteHeroImage(String heroName) throws IOException {
        if (!ALLOWED_HERO_NAMES.contains(heroName)) {
            throw new IOException("Nombre de imagen hero no válido. Permitidos: hero1, hero2, hero3");
        }

        String fileName = heroName + ".webp";

        try {
            String storagePath = "/storage/v1/object/%s/%s/%s".formatted(
                    supabaseStorageProperties.getBucket(),
                    HERO_DIRECTORY,
                    fileName);

            webClient.delete()
                    .uri(storagePath)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Imagen hero eliminada correctamente: {}", fileName);

        } catch (Exception e) {
            log.error("Error al eliminar imagen hero de Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al eliminar la imagen hero de Supabase Storage", e);
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
