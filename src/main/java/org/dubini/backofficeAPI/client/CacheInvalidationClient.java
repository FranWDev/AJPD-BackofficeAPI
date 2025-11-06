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

    public Mono<HttpResponse> invalidateServiceWorkersCache() {
        String jwt = jwtProvider.generateShortLivedToken();

        return webClient.post()
                .uri("http://localhost:8081/api/service-workers/update")
                .cookie("jwt", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new RuntimeException("Error del servidor al invalidar la caché de los service workers")))
                .bodyToMono(HttpResponse.class)
                .doOnSuccess(resp -> System.out.println("Service workers cache invalidated"))
                .doOnError(err -> System.err.println("Error invalidating service workers cache: " + err.getMessage()));
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