package org.dubini.backofficeAPI.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.dubini.backofficeAPI.dto.PublicationDTO;
import org.dubini.backofficeAPI.dto.response.HttpResponse;
import org.dubini.backofficeAPI.exception.PublicationNotFoundException;
import org.dubini.backofficeAPI.exception.PublicationStorageException;
import org.dubini.backofficeAPI.model.News;
import org.dubini.backofficeAPI.repository.NewsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private static final String SAFE_FILENAME_PATTERN = "[^a-zA-Z0-9-_]";

    private final NewsRepository newsRepository;
    private final ObjectMapper objectMapper;
    private final CacheInvalidatorService cacheInvalidation;

    public Optional<PublicationDTO> get(String identifier) {
        log.debug("Retrieving news with identifier: {}", identifier);

        String safeTitle = sanitizeFileName(identifier);

        return newsRepository.findById(safeTitle)
                .map(this::parseNewsToDTO);
    }

    public List<PublicationDTO> get() {
        log.debug("Retrieving all news");

        List<News> newsList = newsRepository.findAllByOrderByCreatedAtDesc();

        List<PublicationDTO> publications = newsList.stream()
                .map(this::parseNewsToDTO)
                .filter(pub -> pub != null)
                .collect(Collectors.toList());

        log.debug("Retrieved {} news articles", publications.size());
        return publications;
    }

    public void save(PublicationDTO publicationDTO) {
        log.debug("Saving news: {}", publicationDTO.getTitle());

        validatePublication(publicationDTO);

        publicationDTO.setPublishedAt(LocalDateTime.now().toString());

        String safeTitle = sanitizeFileName(publicationDTO.getTitle());

        try {
            String jsonContent = objectMapper.writeValueAsString(publicationDTO);

            // Check if exists and overwrite if it does
            News news = newsRepository.findById(safeTitle)
                    .orElse(new News());

            news.setTitle(safeTitle);
            news.setContent(jsonContent);

            newsRepository.save(news);

            log.info("News saved successfully: {}", publicationDTO.getTitle());
        } catch (JsonProcessingException e) {
            log.error("Error serializing news: {}", publicationDTO.getTitle(), e);
            throw new PublicationStorageException("Error al guardar la noticia", e);
        }

        cacheInvalidation.invalidateNewsCache().subscribe(
                resp -> log.info("News cache invalidated after save"),
                err -> log.error("Error invalidating cache after save: {}", err.getMessage()));
    }

    public void delete(String identifier) {
        log.debug("Deleting news: {}", identifier);

        String safeTitle = sanitizeFileName(identifier);

        if (!newsRepository.existsById(safeTitle)) {
            log.warn("News not found for deletion: {}", identifier);
            throw new PublicationNotFoundException("Noticia no encontrada: " + identifier);
        }

        newsRepository.deleteById(safeTitle);
        log.info("News deleted successfully: {}", identifier);

        cacheInvalidation.invalidateNewsCache().subscribe(
                resp -> log.info("News cache invalidated after delete"),
                err -> log.error("Error invalidating cache after delete: {}", err.getMessage()));
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

    private PublicationDTO parseNewsToDTO(News news) {
        try {
            return objectMapper.readValue(news.getContent(), PublicationDTO.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing news content for: {}", news.getTitle(), e);
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