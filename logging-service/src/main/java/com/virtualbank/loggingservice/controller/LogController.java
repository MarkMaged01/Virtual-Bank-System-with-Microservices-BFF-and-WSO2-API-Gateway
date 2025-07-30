package com.virtualbank.loggingservice.controller;

import com.virtualbank.loggingservice.model.Log;
import com.virtualbank.loggingservice.model.MessageType;
import com.virtualbank.loggingservice.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * REST Controller for log operations
 * Provides endpoints for viewing and managing log entries
 */
@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    /**
     * Get all logs
     * @return List of all log entries
     */
    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        List<Log> logs = logService.getAllLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * Get logs by message type
     * @param messageType The type of message (Request, Response, Error)
     * @return List of logs with the specified type
     */
    @GetMapping("/type/{messageType}")
    public ResponseEntity<List<Log>> getLogsByType(@PathVariable String messageType) {
        try {
            MessageType type = MessageType.valueOf(messageType);
            List<Log> logs = logService.getLogsByType(type);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get logs by date range
     * @param startDate Start date (format: yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate End date (format: yyyy-MM-dd'T'HH:mm:ss)
     * @return List of logs within the date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Log>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Log> logs = logService.getLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    /**
     * Search logs by text content
     * @param searchText Text to search for in log messages
     * @return List of logs containing the search text
     */
    @GetMapping("/search")
    public ResponseEntity<List<Log>> searchLogs(@RequestParam String searchText) {
        List<Log> logs = logService.searchLogsByText(searchText);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get log by ID
     * @param id The log ID
     * @return The log entry if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Log> getLogById(@PathVariable Long id) {
        Optional<Log> log = logService.getLogById(id);
        return log.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get log statistics
     * @return Statistics about log entries
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getLogStats() {
        long totalLogs = logService.getTotalLogCount();
        long requestLogs = logService.countLogsByType(MessageType.Request);
        long responseLogs = logService.countLogsByType(MessageType.Response);
        long errorLogs = logService.countLogsByType(MessageType.Error);

        return ResponseEntity.ok(Map.of(
            "totalLogs", totalLogs,
            "requestLogs", requestLogs,
            "responseLogs", responseLogs,
            "errorLogs", errorLogs
        ));
    }

    /**
     * Delete log by ID
     * @param id The log ID to delete
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        logService.deleteLog(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Logging Service is running!");
    }
} 