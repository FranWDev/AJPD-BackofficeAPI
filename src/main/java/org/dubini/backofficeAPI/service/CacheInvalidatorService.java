package org.dubini.backofficeAPI.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.dubini.backofficeAPI.dto.response.HttpResponse;
import org.dubini.backofficeAPI.client.CacheInvalidationClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheInvalidatorService {

    private final CacheInvalidationClient cacheInvalidation;

    public Mono<HttpResponse> invalidateNewsCache() {
        return cacheInvalidation.invalidateNewsCache()
                .map(resp -> new HttpResponse("News cache invalidated"))
                .onErrorResume(err -> {
                    log.error("Error invalidating news cache", err);
                    return Mono.just(new HttpResponse("Error invalidating news cache"));
                });
    }

    public Mono<HttpResponse> invalidateServiceWorkersCache() {
        return cacheInvalidation.invalidateServiceWorkersCache()
                .map(res -> new HttpResponse("Service worker cache invalidated"))
                .onErrorResume(err -> {
                    log.error("Error invalidating sw cache", err);
                    return Mono.just(new HttpResponse("Error invalidating sw cache"));
                });
    }

}
