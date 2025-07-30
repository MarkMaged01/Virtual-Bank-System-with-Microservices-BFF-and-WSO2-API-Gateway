package com.Ejada.BFF.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, jakarta.servlet.FilterChain chain) throws jakarta.servlet.ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.debug("=== JWT FILTER DEBUG ===");
        logger.debug("Request URI: {}", requestURI);
        logger.debug("Request Method: {}", method);
        
        String token = null;
        String username = null;

        // Check Authorization header
        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", authHeader != null ? "present" : "missing");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.debug("Token extracted from Authorization header, length: {}", token.length());
        }

        // Check cookies (optional)
        if (token == null) {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        logger.debug("Token extracted from cookie, length: {}", token.length());
                        break;
                    }
                }
            }
        }

        if (token != null && jwtUtil.isValidJwtStructure(token)) {
            try {
                username = jwtUtil.extractUsername(token);
                logger.debug("Extracted username from token: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // Extract userId from path - handle both /bff/dashboard/{userId} and /dashboard/{userId}
                    String requestedUserId = extractUserIdFromPath(requestURI);
                    logger.debug("Requested userId from path: {}", requestedUserId);
                    
                    if (jwtUtil.validateToken(token, username) && jwtUtil.validateTokenForUser(token, requestedUserId)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("User authenticated successfully with userId {}: {}", requestedUserId, username);
                    } else {
                        logger.warn("Token or userId validation failed for username: {}, requestedUserId: {}", username, requestedUserId);
                    }
                } else {
                    if (username == null) {
                        logger.warn("Username is null from token");
                    } else {
                        logger.debug("User already authenticated or username is null");
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing token: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        } else {
            if (token == null) {
                logger.debug("No token found in request");
            } else {
                logger.debug("Token has invalid JWT structure");
            }
        }

        logger.debug("=== END JWT FILTER DEBUG ===");
        chain.doFilter(request, response);
    }
    
    private String extractUserIdFromPath(String requestURI) {
        // Handle both /bff/dashboard/{userId} and /dashboard/{userId} patterns
        if (requestURI.startsWith("/bff/dashboard/")) {
            return requestURI.substring("/bff/dashboard/".length());
        } else if (requestURI.startsWith("/dashboard/")) {
            return requestURI.substring("/dashboard/".length());
        }
        return "";
    }
}