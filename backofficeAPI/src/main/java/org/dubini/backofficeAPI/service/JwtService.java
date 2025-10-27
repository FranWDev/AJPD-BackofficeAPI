package org.dubini.backofficeAPI.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
    private final String secretKey = "y2XNO0zZrO6Aj1DdYqJ9GgYMKqUUVH2I3smKckddO0TL9vRQwrVChD3GpAnlz3vkeRHK+4tYvnwRyaqRaS/N4A==";
    private final SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));

    public Date extractExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getBody();
            return claims.getExpiration();
        } catch (JwtException e) {
            System.out.println("ERROR al extraer expiración del token JWT");
            e.printStackTrace();
            return null;
        }
    }

    public long getRemainingValidity(String token) {
        Date expiration = extractExpiration(token);
        if (expiration == null) return -1;

        long now = System.currentTimeMillis();
        long diff = expiration.getTime() - now;
        return diff > 0 ? diff : -1;
    }
}
