package org.dubini.backofficeAPI.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.dubini.backofficeAPI.dto.PublicationDTO;
import org.dubini.backofficeAPI.dto.response.HttpResponse;
import org.dubini.backofficeAPI.service.NewsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<List<PublicationDTO>> get() {
        log.debug("GET request to retrieve all news");
        List<PublicationDTO> newsList = newsService.get();
        return ResponseEntity.ok(newsList);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<PublicationDTO> get(@PathVariable String identifier) {
        log.debug("GET request to retrieve news with identifier: {}", identifier);
        return newsService.get(identifier)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HttpResponse> create(@Valid @RequestBody PublicationDTO publicationDTO) {
        log.debug("POST request to create new news article");
        newsService.save(publicationDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new HttpResponse("Noticia creada correctamente"));
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<HttpResponse> delete(@PathVariable String identifier) {
        log.debug("DELETE request to delete news with identifier: {}", identifier);
        newsService.delete(identifier);
        return ResponseEntity.ok(new HttpResponse("Noticia eliminada correctamente"));
    }
}