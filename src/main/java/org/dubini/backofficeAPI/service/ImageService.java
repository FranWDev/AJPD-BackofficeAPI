package org.dubini.backofficeAPI.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.dubini.backofficeAPI.dto.response.ImageResponseDTO;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class ImageService {

    private final WebClient webClient;
    private final String apiKey;

    public ImageService(
            @Value("${uploadme.base-url:https://uploadme.me}") String uploadMeBaseUrl,
            @Value("${uploadme.api-key}") String uploadMeApiKey) {
        this.apiKey = uploadMeApiKey;
        this.webClient = WebClient.builder()
                .baseUrl(uploadMeBaseUrl)
                .defaultHeader("User-Agent", "DubiniBackoffice/1.0")
                .build();
    }

    public ImageResponseDTO saveImage(MultipartFile file, int width, int height, float quality) throws IOException {
        log.debug("Procesando imagen antes de subir a UploadMe: {}", file.getOriginalFilename());

        // Redimensionar y convertir a webp
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(originalImage)
                .size(width, height)
                .outputFormat("webp")
                .outputQuality(quality)
                .toOutputStream(outputStream);

        byte[] processedImage = outputStream.toByteArray();

        // Cuerpo multipart: UploadMe espera "source"
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("source", new ByteArrayResource(processedImage) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("name", file.getOriginalFilename());

        try {
            ResponseEntity<String> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/1/upload/")
                            .queryParam("key", apiKey)
                            .queryParam("format", "json")
                            .queryParam("expiration", 0)
                            .build())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .toEntity(String.class)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.error("UploadMe error: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Error al contactar con UploadMe"));
                    })
                    .block();

            if (response == null || response.getBody() == null) {
                throw new IOException("Respuesta nula o vacía de UploadMe");
            }

            String responseBody = response.getBody();
            log.info("📦 UploadMe respuesta: {}", responseBody);

            JSONObject json = new JSONObject(responseBody);

            // Leer el subobjeto "image"
            JSONObject imageObject = json.optJSONObject("image");
            if (imageObject == null) {
                log.error("UploadMe no devolvió un objeto 'image': {}", json);
                throw new IOException("No se recibió objeto 'image' válido desde UploadMe");
            }

            String imageUrl = imageObject.optString("url", null);
            String fileName = imageObject.optString("filename", file.getOriginalFilename());
            long fileSize = imageObject.optLong("size", processedImage.length);

            if (imageUrl == null || imageUrl.isBlank()) {
                throw new IOException("UploadMe no devolvió una URL válida");
            }

            log.info("Imagen subida correctamente: {}", imageUrl);
            return new ImageResponseDTO(fileName, imageUrl, fileSize);

        } catch (Exception e) {
            log.error("Falló la subida a UploadMe: {}", e.getMessage(), e);
            throw new IOException("Error al subir la imagen a UploadMe", e);
        }
    }
}
