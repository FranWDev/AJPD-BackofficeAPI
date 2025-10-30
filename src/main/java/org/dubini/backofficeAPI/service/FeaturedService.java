package org.dubini.backofficeAPI.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dubini.backofficeAPI.dto.PublicationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeaturedService {

    private static final String JSON_EXTENSION = ".json";
    private final ObjectMapper objectMapper;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private final CacheInvalidatorService cacheInvalidatorService;

    @Value("${app.storage.news.path:storage/news}")
    private String newsStoragePath;
    @Value("${app.storage.activities.path:storage/activities}")
    private String activitiesStoragePath;

    public List<PublicationDTO> get() {
        Path newsDirectory = Paths.get(newsStoragePath);
        Path activitiesDirectory = Paths.get(activitiesStoragePath);

        if (!Files.exists(newsDirectory) || !Files.exists(activitiesDirectory)) {
            log.warn("Activities or news directory does not exist: {}, {}", activitiesDirectory, newsDirectory);
            return List.of();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

        try (
                Stream<Path> newsStream = Files.list(newsDirectory);
                Stream<Path> activitiesStream = Files.list(activitiesDirectory)) {
            return Stream.concat(newsStream, activitiesStream)
                    .filter(path -> path.toString().endsWith(JSON_EXTENSION))
                    .map(this::readPublicationFromFile)
                    .filter(pub -> pub != null && pub.getPublishedAt() != null)
                    .sorted(Comparator.comparing(
                            pub -> {
                                try {
                                    return LocalDateTime.parse(pub.getPublishedAt(), formatter);
                                } catch (Exception e) {
                                    return LocalDateTime.MIN; // Si falla el parseo, lo manda al final
                                }
                            },
                            Comparator.reverseOrder() // Más reciente primero
                    ))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error reading publications from directories: {}, {}", newsDirectory, activitiesDirectory, e);
            return List.of();
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
}