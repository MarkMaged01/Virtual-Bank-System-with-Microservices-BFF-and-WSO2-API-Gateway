package com.john.Ejada.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
//The toolbox for creating and validating tokens.
@Component
public class JwtUtil {
    // Use a fixed secret key for consistency across service restarts
    private final Key SECRET_KEY = Keys.hmacShaKeyFor("your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm".getBytes());
    private final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public String generateToken(String username, String userId) {
        logger.debug("Generating token for username: {} with userId: {}", username, userId);
        String token = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        int periodCount = token.split("\\.").length - 1;
        logger.debug("Generated token: {} (period count: {})", token, periodCount);
        if (periodCount != 2) {
            logger.error("Invalid token generated for username {}: Expected 2 periods, found {}", username, periodCount);
        }
        return token;
    }

    public String generateToken(String username) {
        logger.debug("Generating token for username: {} (legacy method)", username);
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
        int periodCount = token.split("\\.").length - 1;
        logger.debug("Generated token: {} (period count: {})", token, periodCount);
        if (periodCount != 2) {
            logger.error("Invalid token generated for username {}: Expected 2 periods, found {}", username, periodCount);
        }
        return token;
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

    public boolean validateToken(String token) {
        try {
            logger.debug("=== JWT VALIDATION DEBUG ===");
            logger.debug("Validating token: {}", token);
            boolean isExpired = isTokenExpired(token);
            logger.debug("Token expired: {}", isExpired);
            boolean isValid = !isExpired;
            logger.debug("Token validation result: {}", isValid);
            logger.debug("=== END JWT VALIDATION DEBUG ===");
            return isValid;
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            logger.error("Exception details: ", e);
            return false;
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public boolean isValidJwtStructure(String token) {
        if (token == null) {
            logger.error("Token is null");
            return false;
        }
        int periodCount = token.split("\\.").length - 1;
        boolean isValid = periodCount == 2;
        logger.debug("Checking JWT structure for token: {} (period count: {}, valid: {})", token, periodCount, isValid);
        return isValid;
    }

    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", String.class);
        } catch (Exception e) {
            logger.error("Error extracting userId from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateTokenForUser(String token, String requestedUserId) {
        try {
            logger.debug("=== JWT USER VALIDATION DEBUG ===");
            logger.debug("Validating token for requested userId: {}", requestedUserId);
            
            // First validate token structure and expiration
            if (!validateToken(token)) {
                logger.debug("Token validation failed");
                return false;
            }
            
            // Extract userId from token
            String tokenUserId = extractUserId(token);
            logger.debug("UserId from token: {}", tokenUserId);
            
            // Compare userIds
            boolean isValid = requestedUserId.equals(tokenUserId);
            logger.debug("UserId match: {}", isValid);
            logger.debug("=== END JWT USER VALIDATION DEBUG ===");
            
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating token for user: {}", e.getMessage());
            return false;
        }
    }
}