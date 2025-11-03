package org.dubini.backofficeAPI.controller;

import java.util.List;

import org.dubini.backofficeAPI.dto.PublicationDTO;
import org.dubini.backofficeAPI.service.FeaturedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@RestController 
@RequestMapping("/api/featured")
public class FeaturedController {

    private final FeaturedService featuredService;

    @GetMapping
    public ResponseEntity<List<PublicationDTO>> get() {
        log.debug("GET request to retrieve all featured");
        List<PublicationDTO> newsList = featuredService.get();
        return ResponseEntity.ok(newsList);
    }
}
