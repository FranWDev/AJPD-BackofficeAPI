package org.dubini.backofficeAPI.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.dubini.backofficeAPI.dto.PublicationDTO;
import org.dubini.backofficeAPI.dto.response.HttpResponse;
import org.dubini.backofficeAPI.exception.PublicationNotFoundException;
import org.dubini.backofficeAPI.exception.PublicationStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private static final String JSON_EXTENSION = ".json";
    private static final String SAFE_FILENAME_PATTERN = "[^a-zA-Z0-9-_]";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final ObjectMapper objectMapper;
    private final CacheInvalidatorService cacheInvalidation;

    @Value("${app.storage.news.path:storage/news}")
    private String storagePath;

    public Optional<PublicationDTO> get(String identifier) {
        log.debug("Retrieving news with identifier: {}", identifier);

        Path filePath = buildFilePath(identifier);

        if (!Files.exists(filePath)) {
            log.warn("News not found: {}", identifier);
            return Optional.empty();
        }

        try {
            PublicationDTO publication = objectMapper.readValue(filePath.toFile(), PublicationDTO.class);
            log.debug("News retrieved successfully: {}", identifier);
            return Optional.of(publication);
        } catch (IOException e) {
            log.error("Error reading news file: {}", identifier, e);
            throw new PublicationStorageException("Error al leer la noticia", e);
        }
    }

    public List<PublicationDTO> get() {
        log.debug("Retrieving all news");

        Path directory = Paths.get(storagePath);

        if (!Files.exists(directory)) {
            log.warn("News directory does not exist: {}", storagePath);
            return new ArrayList<>();
        }

        try {
            List<PublicationDTO> publications = Files.list(directory)
                    .filter(path -> path.toString().endsWith(JSON_EXTENSION))
                    .map(path -> (PublicationDTO) this.readPublicationFromFile(path))
                    .filter(pub -> pub != null && pub.getPublishedAt() != null)
                    .sorted(Comparator.comparing(
                            PublicationDTO::getPublishedAtDateTime,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ))
                    .collect(Collectors.toList());

            log.debug("Retrieved {} news articles", publications.size());
            return publications;
        } catch (IOException e) {
            log.error("Error listing news", e);
            throw new PublicationStorageException("Error al listar las noticias", e);
        }
    }

    public void save(PublicationDTO publicationDTO) {
        log.debug("Saving new news: {}", publicationDTO.getTitle());

        validatePublication(publicationDTO);

        publicationDTO.setPublishedAt(LocalDateTime.now().toString());

        Path filePath = buildFilePath(publicationDTO.getTitle());

        ensureDirectoryExists();

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), publicationDTO);
            log.info("News saved successfully: {}", publicationDTO.getTitle());
        } catch (IOException e) {
            log.error("Error saving news: {}", publicationDTO.getTitle(), e);
            throw new PublicationStorageException("Error al guardar la noticia", e);
        }
        cacheInvalidation.invalidateNewsCache().subscribe(
                resp -> log.info("News cache invalidated after save"),
                err -> log.error("Error invalidating cache after save: {}", err.getMessage()));
    }

    public void delete(String identifier) {
        log.debug("Deleting news: {}", identifier);

        Path filePath = buildFilePath(identifier);

        if (!Files.exists(filePath)) {
            log.warn("News not found for deletion: {}", identifier);
            throw new PublicationNotFoundException("Noticia no encontrada: " + identifier);
        }

        try {
            Files.delete(filePath);
            log.info("News deleted successfully: {}", identifier);
        } catch (IOException e) {
            log.error("Error deleting news: {}", identifier, e);
            throw new PublicationStorageException("Error al eliminar la noticia", e);
        }
        cacheInvalidation.invalidateNewsCache().subscribe(
                resp -> log.info("News cache invalidated after save"),
                err -> log.error("Error invalidating cache after save: {}", err.getMessage()));
    }

    private Path buildFilePath(String identifier) {
        String safeFileName = sanitizeFileName(identifier);
        return Paths.get(storagePath, safeFileName + JSON_EXTENSION);
    }

    private String sanitizeFileName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        String sanitized = filename.replaceAll(SAFE_FILENAME_PATTERN, "_");

        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }

        return sanitized;
    }

    private void ensureDirectoryExists() {
        Path directory = Paths.get(storagePath);

        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
                log.info("Created news storage directory: {}", storagePath);
            } catch (IOException e) {
                log.error("Error creating storage directory: {}", storagePath, e);
                throw new PublicationStorageException("Error al crear el directorio de almacenamiento", e);
            }
        }
    }

    private PublicationDTO readPublicationFromFile(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), PublicationDTO.class);
        } catch (IOException e) {
            log.error("Error reading file: {}", path, e);
            return null;
        }
    }

    private void validatePublication(PublicationDTO publicationDTO) {
        if (publicationDTO == null) {
            throw new IllegalArgumentException("La noticia no puede ser nula");
        }

        if (publicationDTO.getTitle() == null || publicationDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
    }

    public Mono<HttpResponse> invalidateNewsCache() {
        return cacheInvalidation.invalidateNewsCache();
    }
}