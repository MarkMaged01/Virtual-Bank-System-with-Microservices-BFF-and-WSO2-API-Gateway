package com.virtualbank.loggingservice.service;

import com.virtualbank.loggingservice.dto.LogMessageDto;
import com.virtualbank.loggingservice.model.Log;
import com.virtualbank.loggingservice.model.MessageType;
import com.virtualbank.loggingservice.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for log operations
 * Handles business logic for saving and retrieving log entries
 */
@Service
public class LogService {

    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Save a log message to the database
     * @param logMessageDto The log message data to save
     * @return The saved log entity
     */
    public Log saveLog(LogMessageDto logMessageDto) {
        try {
            Log log = new Log(
                logMessageDto.getMessage(),
                logMessageDto.getMessageType(),
                logMessageDto.getDateTime() != null ? logMessageDto.getDateTime() : LocalDateTime.now()
            );
            
            Log savedLog = logRepository.save(log);
            logger.info("Log saved successfully with ID: {}", savedLog.getId());
            return savedLog;
        } catch (Exception e) {
            logger.error("Error saving log: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save log", e);
        }
    }

    /**
     * Save a log message with current timestamp
     * @param message The log message content
     * @param messageType The type of log message
     * @return The saved log entity
     */
    public Log saveLog(String message, MessageType messageType) {
        LogMessageDto dto = new LogMessageDto(message, messageType, LocalDateTime.now());
        return saveLog(dto);
    }

    /**
     * Find all logs
     * @return List of all log entries
     */
    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }

    /**
     * Find logs by message type
     * @param messageType The type of message to search for
     * @return List of logs with the specified message type
     */
    public List<Log> getLogsByType(MessageType messageType) {
        return logRepository.findByMessageType(messageType);
    }

    /**
     * Find logs by date range
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of logs within the specified date range
     */
    public List<Log> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.findByDateTimeBetween(startDate, endDate);
    }

    /**
     * Find logs by message type and date range
     * @param messageType The type of message to search for
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of logs with specified type within the date range
     */
    public List<Log> getLogsByTypeAndDateRange(MessageType messageType, LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.findByMessageTypeAndDateTimeBetween(messageType, startDate, endDate);
    }

    /**
     * Find logs containing specific text
     * @param searchText Text to search for in the message field
     * @return List of logs containing the search text
     */
    public List<Log> searchLogsByText(String searchText) {
        return logRepository.findByMessageContaining(searchText);
    }

    /**
     * Get log by ID
     * @param id The log ID
     * @return Optional containing the log if found
     */
    public Optional<Log> getLogById(Long id) {
        return logRepository.findById(id);
    }

    /**
     * Count logs by message type
     * @param messageType The type of message to count
     * @return Number of logs with the specified message type
     */
    public long countLogsByType(MessageType messageType) {
        return logRepository.countByMessageType(messageType);
    }

    /**
     * Get total count of all logs
     * @return Total number of log entries
     */
    public long getTotalLogCount() {
        return logRepository.count();
    }

    /**
     * Delete log by ID
     * @param id The log ID to delete
     */
    public void deleteLog(Long id) {
        if (logRepository.existsById(id)) {
            logRepository.deleteById(id);
            logger.info("Log deleted successfully with ID: {}", id);
        } else {
            logger.warn("Attempted to delete non-existent log with ID: {}", id);
        }
    }
} 