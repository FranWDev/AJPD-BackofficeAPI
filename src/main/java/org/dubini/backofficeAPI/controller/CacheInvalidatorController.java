package org.dubini.backofficeAPI.controller;

import org.dubini.backofficeAPI.dto.response.HttpResponse;
import org.dubini.backofficeAPI.service.CacheInvalidatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cache")
public class CacheInvalidatorController {

    private final CacheInvalidatorService cacheInvalidatorService;

    @GetMapping("/invalidate/news")
    public Mono<HttpResponse> invalidateNewsCache() {
        return cacheInvalidatorService.invalidateNewsCache();
    }
/* 
    @GetMapping("/invalidate/activities")
    public Mono<HttpResponse> invalidateActivitiesCache() {
        return cacheInvalidatorService.invalidateActivitiesCache();
    }*/
}