package com.john.Ejada.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private String user_id;
    private String username;
    private String message;
    private String email;
    private String first_name;
    private String last_name;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public void setMessage(String s) {
        this.message = s;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
}