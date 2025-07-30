package com.john.Ejada.Config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

//Extract JWT	From Authorization header
//Decode & Validate	Get username and check token validity
//Set Authentication	Put auth info in SecurityContext
//Continue	Let request continue to controller
//the gatekeeper that checks each request for a valid token

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    jakarta.servlet.FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header for request {}: {}", request.getRequestURI(), authHeader);
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.debug("Extracted JWT from Authorization header: {}", token);
        } else {
            logger.debug("No Authorization header or invalid format for request: {}", request.getRequestURI());
        }

        if (token != null) {
            try {
                logger.debug("=== JWT FILTER DEBUG ===");
                logger.debug("Token to validate: {}", token);
                String username = jwtUtil.extractUsername(token);
                logger.debug("Extracted username from token: {}", username);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.debug("About to validate token...");
                    boolean isValid = jwtUtil.validateToken(token);
                    logger.debug("Token validation result: {}", isValid);
                    if (isValid) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Set authentication for username: {}", username);
                    } else {
                        logger.debug("Invalid JWT token for username: {}", username);
                    }
                } else {
                    logger.debug("No username extracted or authentication already set");
                }
                logger.debug("=== END JWT FILTER DEBUG ===");
            } catch (Exception e) {
                logger.error("Error processing JWT token: {}", e.getMessage());
                logger.error("Exception details: ", e);
            }
        }
        String appName = request.getHeader("APP-NAME");
        System.out.println("APP-NAME Header: " + appName);
        filterChain.doFilter(request, response);
        
    }
}