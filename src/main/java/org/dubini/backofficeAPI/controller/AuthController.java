package org.dubini.backofficeAPI.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.dubini.backofficeAPI.dto.request.LoginRequest;
import org.dubini.backofficeAPI.dto.response.JwtResponse;
import org.dubini.backofficeAPI.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(
            @CookieValue(value = "jwt", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.debug("Token refresh request received");

        String jwtToken = extractToken(token, authHeader);

        if (jwtToken == null || jwtToken.isEmpty()) {
            log.warn("Refresh attempt without valid token");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        String newToken = authService.refreshToken(jwtToken);
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(new JwtResponse(newToken));
    }

    private String extractToken(String cookieToken, String authHeader) {
        if (cookieToken != null && !cookieToken.isEmpty()) {
            return cookieToken;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}