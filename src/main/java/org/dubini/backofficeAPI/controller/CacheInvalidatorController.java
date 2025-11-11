package org.dubini.backofficeAPI.controller;

import org.dubini.backofficeAPI.dto.response.HttpResponse;
import org.dubini.backofficeAPI.service.CacheInvalidatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cache")
public class CacheInvalidatorController {

    private final CacheInvalidatorService cacheInvalidatorService;

    @GetMapping("/invalidate/news")
    public HttpResponse invalidateNewsCache() {
        return cacheInvalidatorService.invalidateNewsCache().block();
    }

    @GetMapping("/invalidate/service-workers")
    public HttpResponse invalidateServiceWorkersCache() {
        return cacheInvalidatorService.invalidateServiceWorkersCache().block();
    }
    /*
     * @GetMapping("/invalidate/activities")
     * public Mono<HttpResponse> invalidateActivitiesCache() {
     * return cacheInvalidatorService.invalidateActivitiesCache();
     * }
     */
}