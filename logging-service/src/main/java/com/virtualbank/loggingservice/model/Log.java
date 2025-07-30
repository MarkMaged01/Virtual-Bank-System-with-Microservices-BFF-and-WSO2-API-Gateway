package com.virtualbank.loggingservice.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a log entry in the dump table
 */
@Entity
@Table(name = "dump", indexes = {
    @Index(name = "idx_message_type", columnList = "message_type"),
    @Index(name = "idx_date_time", columnList = "date_time"),
    @Index(name = "idx_message_type_date", columnList = "message_type, date_time")
})
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 10)
    private MessageType messageType;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    // Default constructor
    public Log() {
    }

    // Constructor with all fields
    public Log(String message, MessageType messageType, LocalDateTime dateTime) {
        this.message = message;
        this.messageType = messageType;
        this.dateTime = dateTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        return "Log{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", messageType=" + messageType +
                ", dateTime=" + dateTime +
                '}';
    }
} 