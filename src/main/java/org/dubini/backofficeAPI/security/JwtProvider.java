package org.dubini.backofficeAPI.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.dubini.backofficeAPI.config.JwtProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long jwtExpiration;


    public JwtProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(props.getSecret()));
        this.jwtExpiration = 86400000L;
    }


    public String generateToken() {
        @SuppressWarnings("deprecation")
        String token = Jwts.builder()
                .setSubject("backoffice")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();

        System.out.println("=== TOKEN GENERATED ===");
        System.out.println("Token: " + token);
        System.out.println("Expiration: " + new Date(System.currentTimeMillis() + jwtExpiration));

        return token;
    }

    public String generateShortLivedToken() {
        long shortLivedExpiration = 30_000L;
        @SuppressWarnings("deprecation")
        String token = Jwts.builder()
                .setSubject("backoffice")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + shortLivedExpiration))
                .signWith(key)
                .compact();

        System.out.println("=== SHORT-LIVED TOKEN GENERATED ===");
        System.out.println("Token: " + token);
        System.out.println("Expiration: " + new Date(System.currentTimeMillis() + shortLivedExpiration));

        return token;
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("--- Token Validation Start ---");
            System.out.println("Token to validate: " + token.substring(0, Math.min(50, token.length())) + "...");

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            Date expiration = claims.getExpiration();
            Date now = new Date();

            System.out.println("Subject: " + subject);
            System.out.println("Expiration: " + expiration);
            System.out.println("Current time: " + now);
            System.out.println("Is expired: " + expiration.before(now));
            System.out.println("Subject matches 'backoffice': " + "backoffice".equals(subject));

            boolean isValid = "backoffice".equals(subject) && expiration.after(now);
            System.out.println("Final validation result: " + isValid);
            System.out.println("--- Token Validation End ---");

            return isValid;
        } catch (ExpiredJwtException e) {
            System.out.println("ERROR: Token expired");
            System.out.println("Expiration was: " + e.getClaims().getExpiration());
            e.printStackTrace();
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("ERROR: Malformed JWT token");
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            System.out.println("ERROR: Invalid JWT signature");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("ERROR: Token validation failed with exception: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
