package com.john.Ejada.Controllers;

import com.john.Ejada.DTO.LoginRequestDTO;
import com.john.Ejada.DTO.UserRequestDTO;
import com.john.Ejada.DTO.UserResponseDTO;
import com.john.Ejada.Entity.UserEntity;
import com.john.Ejada.Config.JwtUtil;
import com.john.Ejada.exception.InvalidCredentialsException;
import com.john.Ejada.exception.UserAlreadyExistsException;
import com.john.Ejada.exception.UserNotFoundException;
import com.john.Ejada.repositry.UserRepository;
import com.john.Ejada.service.UserService;
import com.john.Ejada.service.LoggingService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final LoggingService loggingService;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil, UserRepository userRepository, LoggingService loggingService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.loggingService = loggingService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO registerUser(@Valid @RequestBody UserRequestDTO userRequest) {
        // Log the request
        loggingService.logRequest("UserService", "/api/users/register", userRequest);
        
        try {
            UserResponseDTO response = userService.registerUser(
                userRequest.getUsername(),
                userRequest.getPassword(),
                userRequest.getEmail(),
                userRequest.getFirstName(),
                userRequest.getLastName()
            );
            
            // Log the response
            loggingService.logResponse("UserService", "/api/users/register", response);
            
            return response;
        } catch (Exception e) {
            // Log the error
            loggingService.logError("UserService", "/api/users/register", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        // Log the request
        loggingService.logRequest("UserService", "/api/users/login", loginRequest);
        
        try {
            logger.debug("Login attempt for username: {}", loginRequest.getUsername());
            UserResponseDTO userResponse = userService.loginUser(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            );
            String jwtToken = userService.generateJwtToken(loginRequest.getUsername());
            logger.debug("Generated JWT for user {}: {}", loginRequest.getUsername(), jwtToken);

            userResponse.setToken(jwtToken);
            
            // Log the response
            loggingService.logResponse("UserService", "/api/users/login", userResponse);
            
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            // Log the error
            loggingService.logError("UserService", "/api/users/login", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{userId}/profile")
    public UserResponseDTO getUserProfile(@PathVariable String userId, @RequestHeader("Authorization") String authHeader) {
        // Log the request
        loggingService.logRequest("UserService", "/api/users/" + userId + "/profile", Map.of("userId", userId));
        
        try {
            logger.debug("Received request for user profile with userId: {}", userId);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                logger.debug("Authorization header token: {}", token);
                
                // Validate token structure and expiration
                if (!jwtUtil.isValidJwtStructure(token)) {
                    logger.error("Invalid JWT structure in token: {}", token);
                    throw new IllegalArgumentException("Invalid JWT token structure");
                }
                
                // Validate that the token belongs to the requested user
                if (!jwtUtil.validateTokenForUser(token, userId)) {
                    logger.error("Token does not match requested user. Token userId: {}, Requested userId: {}", 
                        jwtUtil.extractUserId(token), userId);
                    throw new IllegalArgumentException("Access denied: Token does not match requested user");
                }
                
                // Token is valid and matches the requested user, get the user profile
                UserResponseDTO userProfile = userService.getUserProfile(userId);
                logger.debug("Successfully retrieved profile for user: {}", userId);
                
                // Log the response
                loggingService.logResponse("UserService", "/api/users/" + userId + "/profile", userProfile);
                
                return userProfile;
            } else {
                logger.error("Missing or invalid Authorization header: {}", authHeader);
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
        } catch (Exception e) {
            // Log the error
            loggingService.logError("UserService", "/api/users/" + userId + "/profile", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Map<String, Object>> checkUserExists(@PathVariable String userId) {
        // Log the request
        loggingService.logRequest("UserService", "/api/users/" + userId + "/exists", Map.of("userId", userId));
        
        try {
            logger.debug("Checking if user exists with userId: {}", userId);
            // First check if the user exists in the repository
            boolean exists = userRepository.existsById(userId);
            logger.debug("User with ID {} exists in repository: {}", userId, exists);
            
            if (exists) {
                UserResponseDTO user = userService.getUserInfoWithoutAuth(userId);
                Map<String, Object> response = Map.of(
                    "exists", true,
                    "userId", user.getUser_id(),
                    "username", user.getUsername()
                );
                
                // Log the response
                loggingService.logResponse("UserService", "/api/users/" + userId + "/exists", response);
                
                return ResponseEntity.ok(response);
            } else {
                logger.debug("User with ID {} does not exist in repository", userId);
                Map<String, Object> response = Map.of("exists", false);
                
                // Log the response
                loggingService.logResponse("UserService", "/api/users/" + userId + "/exists", response);
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Error checking user existence for ID {}: {}", userId, e.getMessage());
            Map<String, Object> response = Map.of("exists", false, "error", e.getMessage());
            
            // Log the error
            loggingService.logError("UserService", "/api/users/" + userId + "/exists", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/debug/all-users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        // Log the request
        loggingService.logRequest("UserService", "/api/users/debug/all-users", Map.of());
        
        try {
            logger.debug("Retrieving all users for debugging");
            var users = userRepository.findAll();
            var userList = users.stream()
                .map(user -> Map.of(
                    "userId", user.getUser_Id(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
                ))
                .toList();
            
            Map<String, Object> response = Map.of(
                "totalUsers", users.size(),
                "users", userList
            );
            
            // Log the response
            loggingService.logResponse("UserService", "/api/users/debug/all-users", response);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving all users: {}", e.getMessage());
            Map<String, Object> response = Map.of("error", e.getMessage());
            
            // Log the error
            loggingService.logError("UserService", "/api/users/debug/all-users", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }
}