package com.john.Ejada.service;

import com.john.Ejada.Entity.UserEntity;
import com.john.Ejada.DTO.UserResponseDTO;
import com.john.Ejada.exception.InvalidCredentialsException;
import com.john.Ejada.exception.UserAlreadyExistsException;
import com.john.Ejada.exception.UserNotFoundException;
import com.john.Ejada.repositry.UserRepository;
// import jdk.internal.org.jline.utils.Log;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.john.Ejada.Config.JwtUtil;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponseDTO registerUser(String username, String rawPassword, String email, String firstName, String lastName) {
        UserResponseDTO response = new UserResponseDTO();
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Username or email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        UserEntity savedUser = userRepository.save(user);

        response.setUserId(savedUser.getUser_Id());
        response.setUsername(savedUser.getUsername());
        response.setMessage("User registered successfully.");
        return response;
    }

    public UserResponseDTO loginUser(String username, String password) {
        logger.debug("Attempting login for username: {}, input password: {}", username, password);
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            logger.debug("User not found: {}", username);
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        UserEntity user = userOptional.get();
        logger.debug("Stored password for user {}: {}", username, user.getPassword());
        boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
        logger.debug("Password match for user {}: {}", username, passwordMatches);
        if (!passwordMatches) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(user.getUser_Id());
        response.setUsername(user.getUsername());
        return response;
    }
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public UserResponseDTO getUserProfile(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(user.getUser_Id());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirst_name(user.getFirstName());
        response.setLast_name(user.getLastName());
        return response;
    }

    public UserResponseDTO getUserInfoWithoutAuth(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(user.getUser_Id());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirst_name(user.getFirstName());
        response.setLast_name(user.getLastName());
        return response;
    }

    public String generateJwtToken(String username) {
        // Get user ID for the username
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            String userId = userOptional.get().getUser_Id();
            logger.debug("Generating JWT token for username: {} with userId: {}", username, userId);
            return jwtUtil.generateToken(username, userId);
        } else {
            logger.error("User not found for username: {}", username);
            throw new UserNotFoundException("User not found for username: " + username);
        }
    }
}