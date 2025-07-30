package com.john.Ejada.repositry;

import com.john.Ejada.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<UserEntity> findByUsername(@Param("username") String username);
}