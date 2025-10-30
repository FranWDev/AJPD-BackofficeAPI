package org.dubini.backofficeAPI.client;

import org.dubini.backofficeAPI.security.JwtProvider;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.dubini.backofficeAPI.dto.response.HttpResponse;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class CacheInvalidationClient {

    private final JwtProvider jwtProvider;
    private final WebClient webClient;

    public Mono<HttpResponse> invalidateNewsCache() {
        String jwt = jwtProvider.generateShortLivedToken();

        return webClient.get()
                .uri("http://localhost:8081/api/cache/news/clear")
                .cookie("jwt", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new RuntimeException("Error del servidor al invalidar la caché de noticias")))
                .bodyToMono(HttpResponse.class)
                .doOnSuccess(resp -> System.out.println("News cache invalidated"))
                .doOnError(err -> System.err.println("Error invalidating news cache: " + err.getMessage()));
    }

    public Mono<HttpResponse> invalidateActivitiesCache() {
        String jwt = jwtProvider.generateShortLivedToken();

        return webClient.get()
                .uri("http://localhost:8081/api/cache/activities/clear")
                .cookie("jwt", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    return Mono.error(new RuntimeException("Error del servidor al invalidar la caché de actividades"));
                })
                .bodyToMono(HttpResponse.class)
                .doOnSuccess(resp -> System.out.println("Activities cache invalidated"))
                .doOnError(err -> System.err.println("Error invalidating activities cache: " + err.getMessage()));
    }

    public Mono<HttpResponse> invalidateFeaturedCache() {
        String jwt = jwtProvider.generateShortLivedToken();
        return webClient.get()
                .uri("http://localhost:8081/api/cache/feature/clear")
                .cookie("jwt", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    return Mono.error(new RuntimeException("Error del servidor al invalidar la cache de destacados"));
                })
                .bodyToMono(HttpResponse.class)
                .doOnSuccess(resp -> System.out.println("featured cache invalidated"))
                .doOnError(err -> System.out.println("Error invalidating featured cache"));
    }
    
/*
    public Mono<HttpResponse> invalidateHeroSliderCache() {
        String jwt = jwtProvider.generateToken();

        return webClient.get()
                .uri("http://localhost:8080/api/cache/heroslider/clear")
                .cookie("jwt", jwt)
                .retrieve()
                .bodyToMono(HttpResponse.class);
    }
    */
/* 
    public Mono<HttpResponse> invalidateFeaturedCache() {
        String jwt = jwtProvider.generateToken();

        return webClient.get()
                .uri("http://localhost:8080/api/cache/featured/clear")
                .cookie("jwt", jwt)
                .retrieve()
                .bodyToMono(HttpResponse.class);
    }
*/
}