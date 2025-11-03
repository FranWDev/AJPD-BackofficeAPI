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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivitiesService {

    private static final String JSON_EXTENSION = ".json";
    private static final String SAFE_FILENAME_PATTERN = "[^a-zA-Z0-9-_]";

    private final ObjectMapper objectMapper;
    private final CacheInvalidatorService cacheInvalidation;

    @Value("${app.storage.activities.path:storage/activities}")
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
        log.debug("Retrieving all activities");

        Path directory = Paths.get(storagePath);

        if (!Files.exists(directory)) {
            log.warn("Activities directory does not exist: {}", storagePath);
            return new ArrayList<>();
        }

        try {
            List<PublicationDTO> publications = Files.list(directory)
                    .filter(path -> path.toString().endsWith(JSON_EXTENSION))
                    .map(this::readPublicationFromFile)
                    .filter(pub -> pub != null)
                    .collect(Collectors.toList());

            log.debug("Retrieved {} activities", publications.size());
            return publications;
        } catch (IOException e) {
            log.error("Error listing activities", e);
            throw new PublicationStorageException("Error al listar las publicaciones", e);
        }
    }

    public void save(PublicationDTO publicationDTO) {
        log.debug("Saving new activity: {}", publicationDTO.getTitle());

        validatePublication(publicationDTO);

        publicationDTO.setPublishedAt(LocalDateTime.now().toString());

        Path filePath = buildFilePath(publicationDTO.getTitle());

        ensureDirectoryExists();

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), publicationDTO);
            log.info("Activity saved successfully: {}", publicationDTO.getTitle());
        } catch (IOException e) {
            log.error("Error saving activity: {}", publicationDTO.getTitle(), e);
            throw new PublicationStorageException("Error al guardar la publicación", e);
        }

        cacheInvalidation.invalidateActivitiesCache().subscribe(
                resp -> log.info("Activities cache invalidated after save"),
                err -> log.error("Error invalidating cache after save: {}", err.getMessage()));
    }

    public void delete(String identifier) {
        log.debug("Deleting activity: {}", identifier);

        Path filePath = buildFilePath(identifier);

        if (!Files.exists(filePath)) {
            log.warn("Activity not found for deletion: {}", identifier);
            throw new PublicationNotFoundException("Publicación no encontrada: " + identifier);
        }

        try {
            Files.delete(filePath);
            log.info("Activity deleted successfully: {}", identifier);
        } catch (IOException e) {
            log.error("Error deleting activity: {}", identifier, e);
            throw new PublicationStorageException("Error al eliminar la publicación", e);
        }
        cacheInvalidation.invalidateActivitiesCache().subscribe(
                resp -> log.info("Activities cache invalidated after delete"),
                err -> log.error("Error invalidating cache after delete: {}", err.getMessage()));
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
                log.info("Created activities storage directory: {}", storagePath);
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
            throw new IllegalArgumentException("La publicación no puede ser nula");
        }

        if (publicationDTO.getTitle() == null || publicationDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
    }

    public Mono<HttpResponse> invalidateActivitiesCache() {
        return cacheInvalidation.invalidateActivitiesCache();
    }
}