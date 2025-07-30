package com.Ejada.BFF.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKeyString;
    
    private Key SECRET_KEY;

    // Initialize the secret key after the secretKeyString is injected
    private Key getSecretKey() {
        if (SECRET_KEY == null) {
            // Use the same secret key as the user service for compatibility
            SECRET_KEY = Keys.hmacShaKeyFor("your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm".getBytes());
        }
        return SECRET_KEY;
    }

    private final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours

    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            logger.debug("Extracted username from token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            throw e;
        }
    }

    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", String.class);
        } catch (Exception e) {
            logger.error("Error extracting userId from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            boolean expired = expiration.before(new Date());
            logger.debug("Token expiration check: expired={}", expired);
            return expired;
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername.equals(username) && !isTokenExpired(token);
            logger.debug("Token validation for username {}: valid={}, expired={}", username, isValid, isTokenExpired(token));
            return isValid;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateTokenForUser(String token, String requestedUserId) {
        try {
            logger.debug("=== JWT USER VALIDATION DEBUG ===");
            logger.debug("Validating token for requested userId: {}", requestedUserId);

            if (!validateToken(token, extractUsername(token))) {
                logger.debug("Token validation failed");
                return false;
            }

            String tokenUserId = extractUserId(token);
            logger.debug("UserId from token: {}", tokenUserId);

            boolean isValid = requestedUserId.equals(tokenUserId);
            logger.debug("UserId match: {}", isValid);
            logger.debug("=== END JWT USER VALIDATION DEBUG ===");

            return isValid;
        } catch (Exception e) {
            logger.error("Error validating token for user: {}", e.getMessage());
            return false;
        }
    }

    public boolean isValidJwtStructure(String token) {
        if (token == null) {
            logger.error("Token is null");
            return false;
        }
        int periodCount = token.split("\\.").length - 1;
        boolean isValid = periodCount == 2;
        logger.debug("Checking JWT structure: period count={}, valid={}", periodCount, isValid);
        return isValid;
    }
}