package org.dubini.backofficeAPI.controller;

import org.dubini.backofficeAPI.service.CacheInvalidatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cache")
public class CacheInvalidatorController {

    private final CacheInvalidatorService cacheInvalidatorService;

    @GetMapping("/invalidate/news")
    public void invalidateNewsCache() {
        cacheInvalidatorService.invalidateNewsCache().subscribe();
    }

    @GetMapping("/invalidate/activities")
    public void invalidateActivitiesCache() {
        cacheInvalidatorService.invalidateActivitiesCache().subscribe();
    }

}
