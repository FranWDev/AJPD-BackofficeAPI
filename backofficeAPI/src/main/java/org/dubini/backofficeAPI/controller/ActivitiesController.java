package org.dubini.backofficeAPI.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.dubini.backofficeAPI.dto.PublicationDTO;
import org.dubini.backofficeAPI.dto.response.HttpResponse;
import org.dubini.backofficeAPI.service.ActivitiesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivitiesController {

    private final ActivitiesService activitiesService;

    @GetMapping
    public ResponseEntity<List<PublicationDTO>> getAllActivities() {
        log.debug("GET request to retrieve all activities");
        List<PublicationDTO> publications = activitiesService.get();
        return ResponseEntity.ok(publications);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<PublicationDTO> getActivityByIdentifier(@PathVariable String identifier) {
        log.debug("GET request to retrieve activity with identifier: {}", identifier);
        return activitiesService.get(identifier)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HttpResponse> createActivity(@RequestBody PublicationDTO publicationDTO) {
        log.debug("POST request to create new activity");
        activitiesService.save(publicationDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new HttpResponse("Publicación creada correctamente"));
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<HttpResponse> deleteActivity(@PathVariable String identifier) {
        log.debug("DELETE request to delete activity with identifier: {}", identifier);
        activitiesService.delete(identifier);
        return ResponseEntity.ok(new HttpResponse("Publicación eliminada correctamente"));
    }
}