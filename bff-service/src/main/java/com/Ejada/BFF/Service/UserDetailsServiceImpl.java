package com.Ejada.BFF.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user details for username: {}", username);
        
        // For BFF service, we trust the JWT token validation
        // Create a user with USER role for any valid username from JWT
        if (username != null && !username.trim().isEmpty()) {
            UserDetails userDetails = User.withUsername(username)
                    .password("") // No password for token-based auth
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
            
            logger.debug("Created user details for username: {} with role: USER", username);
            return userDetails;
        }
        
        logger.warn("Invalid username provided: {}", username);
        throw new UsernameNotFoundException("User not found: " + username);
    }
}