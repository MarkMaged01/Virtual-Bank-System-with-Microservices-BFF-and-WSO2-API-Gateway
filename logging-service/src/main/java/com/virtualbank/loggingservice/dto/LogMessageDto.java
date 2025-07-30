package com.virtualbank.loggingservice.dto;

import com.virtualbank.loggingservice.model.MessageType;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for log messages
 * Used to transfer log data between different layers of the application
 */
public class LogMessageDto {
    
    private String message;
    private MessageType messageType;
    private LocalDateTime dateTime;

    // Default constructor
    public LogMessageDto() {
    }

    // Constructor with all fields
    public LogMessageDto(String message, MessageType messageType, LocalDateTime dateTime) {
        this.message = message;
        this.messageType = messageType;
        this.dateTime = dateTime;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "LogMessageDto{" +
                "message='" + message + '\'' +
                ", messageType=" + messageType +
                ", dateTime=" + dateTime +
                '}';
    }
} 