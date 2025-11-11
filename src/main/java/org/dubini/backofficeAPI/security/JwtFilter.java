package org.dubini.backofficeAPI.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private boolean isPublicUrl(String path) {
        if (path.equals("/") || path.equals("/login")) {
            return true;
        }

        return path.startsWith("/styles") ||
                path.startsWith("/scripts") ||
                path.startsWith("/images") ||
                path.startsWith("/img") ||
                path.startsWith("/assets") ||
                path.startsWith("/webjars") ||
                path.equals("/favicon.ico") ||
                path.equals("/api/auth/login") ||
                (path.startsWith("/api/auth/") && path.endsWith("/login"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        System.out.println("========================================");
        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("Path: " + path);
        System.out.println("Method: " + request.getMethod());
        System.out.println("Is public URL: " + isPublicUrl(path));

        if (isPublicUrl(path)) {
            System.out.println("Public URL detected - allowing access without authentication");
            System.out.println("========================================");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("--- Cookies in request ---");
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("Cookie: " + cookie.getName() + " = " +
                        (cookie.getName().equals("jwt") ? cookie.getValue() : "[hidden]"));
            }
        } else {
            System.out.println("No cookies found in request");
        }

        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("JWT cookie found!");
                    break;
                }
            }
        }

        if (token == null) {
            System.out.println("ERROR: No JWT token found - returning 401");
            System.out.println("========================================");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("No JWT token found");
            return;
        }

        System.out.println("Validating token...");
        boolean isValid = jwtProvider.validateToken(token);
        System.out.println("Token validation result: " + isValid);

        if (!isValid) {
            System.out.println("ERROR: Invalid JWT token - returning 401");
            System.out.println("========================================");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token");
            return;
        }

        System.out.println("Token valid - setting authentication in SecurityContext");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("backoffice", null,
                Collections.emptyList());
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(auth);

        System.out.println("Authentication set successfully - proceeding with filter chain");
        System.out.println("========================================");

        filterChain.doFilter(request, response);
    }
}