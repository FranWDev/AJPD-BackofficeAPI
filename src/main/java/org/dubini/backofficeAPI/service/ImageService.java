package org.dubini.backofficeAPI.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.dubini.backofficeAPI.config.SupabaseStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {

    private final WebClient webClient;
    private final SupabaseStorageProperties supabaseStorageProperties;

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

    public ImageResponseDTO saveImage(MultipartFile file, int width, int height, float quality) throws IOException {
        log.debug("Procesando imagen antes de subir a Supabase: {}", file.getOriginalFilename());

        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(originalImage)
                .size(width, height)
                .outputFormat("webp")
                .outputQuality(quality)
                .toOutputStream(outputStream);

        byte[] processedImage = outputStream.toByteArray();

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        
        try {
            String storagePath = String.format("/storage/v1/object/%s/%s", 
                    supabaseStorageProperties.getBucket(), 
                    fileName);

            webClient.post()
                    .uri(storagePath)
                    .header("Content-Type", "image/webp")
                    .bodyValue(processedImage)
                    .retrieve()
                    .toBodilessEntity()
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.error("Supabase error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Error al contactar con Supabase Storage"));
                    })
                    .block();

            String imageUrl = String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseStorageProperties.getApi(),
                    supabaseStorageProperties.getBucket(),
                    fileName);

            log.info("Imagen subida correctamente a Supabase: {}", imageUrl);
            
            return new ImageResponseDTO(fileName, imageUrl, processedImage.length);

        } catch (Exception e) {
            log.error("Falló la subida a Supabase: {}", e.getMessage(), e);
            throw new IOException("Error al subir la imagen a Supabase Storage", e);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.webp", timestamp, uuid);
    }

    public void deleteImage(String fileName) throws IOException {
        try {
            String storagePath = String.format("/storage/v1/object/%s/%s",
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