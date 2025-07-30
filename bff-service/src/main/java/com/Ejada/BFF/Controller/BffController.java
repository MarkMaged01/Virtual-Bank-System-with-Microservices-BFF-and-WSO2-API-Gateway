package com.Ejada.BFF.Controller;

import com.Ejada.BFF.DTO.DashboardResponseDto;
import com.Ejada.BFF.Service.BffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bff")
public class BffController {
    private static final Logger logger = LoggerFactory.getLogger(BffController.class);

    @Autowired
    private BffService bffService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("Health check endpoint called");
        return ResponseEntity.ok("BFF Service is running");
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardResponseDto> getDashboard(@PathVariable String userId,
                                                             @RequestHeader("Authorization") String authHeader) {
        logger.debug("=== DASHBOARD REQUEST DEBUG ===");
        logger.debug("Received dashboard request for userId: {}", userId);
        logger.debug("Authorization header present: {}", authHeader != null);
        
        // Validate Authorization header
        if (authHeader == null || authHeader.trim().isEmpty()) {
            logger.warn("Missing Authorization header for userId: {}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Missing Authorization header"));
        }
        
        try {
            UUID uuid = UUID.fromString(userId);
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            
            logger.debug("Processing dashboard request for userId: {} with token length: {}", userId, token.length());
            logger.debug("Token starts with: {}", token.substring(0, Math.min(20, token.length())));
            
            // Use block() to handle the reactive response synchronously
            DashboardResponseDto result = bffService.getDashboardData(uuid, token).block();
            
            if (result != null) {
                logger.debug("Successfully processed dashboard request for userId: {}", userId);
                return ResponseEntity.ok(result);
            } else {
                logger.error("Null result for dashboard request for userId: {}", userId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Failed to retrieve dashboard data"));
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing dashboard request for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve dashboard data due to an issue with downstream services."));
        } finally {
            logger.debug("=== END DASHBOARD REQUEST DEBUG ===");
        }
    }
    
    private DashboardResponseDto createErrorResponse(String message) {
        DashboardResponseDto errorResponse = new DashboardResponseDto();
        errorResponse.setUserId(null);
        errorResponse.setUsername("");
        errorResponse.setEmail("");
        errorResponse.setFirstName("");
        errorResponse.setLastName("");
        errorResponse.setAccounts(List.of());
        return errorResponse;
    }
}