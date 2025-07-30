package com.virtualbank.loggingservice.model;

/**
 * Enum representing the types of log messages that can be stored
 * This matches the ENUM values in the database dump table
 */
public enum MessageType {
    /**
     * Represents incoming requests to microservices
     * Example: API calls, user actions, etc.
     */
    Request,
    
    /**
     * Represents responses from microservices
     * Example: API responses, operation results, etc.
     */
    Response,
    
    /**
     * Represents error messages and exceptions
     * Example: Validation errors, system failures, etc.
     */
    Error
} 