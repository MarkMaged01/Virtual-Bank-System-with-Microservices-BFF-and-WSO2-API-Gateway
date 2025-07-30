package com.virtualbank.loggingservice.repository;

import com.virtualbank.loggingservice.model.Log;
import com.virtualbank.loggingservice.model.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Log entity
 * Provides methods for database operations on log entries
 */
@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    /**
     * Find logs by message type
     * @param messageType The type of message to search for
     * @return List of logs with the specified message type
     */
    List<Log> findByMessageType(MessageType messageType);

    /**
     * Find logs by date range
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of logs within the specified date range
     */
    List<Log> findByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find logs by message type and date range
     * @param messageType The type of message to search for
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return List of logs with specified type within the date range
     */
    List<Log> findByMessageTypeAndDateTimeBetween(MessageType messageType, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Custom query to find logs containing specific text in the message
     * @param searchText Text to search for in the message field
     * @return List of logs containing the search text
     */
    @Query("SELECT l FROM Log l WHERE l.message LIKE %:searchText%")
    List<Log> findByMessageContaining(@Param("searchText") String searchText);

    /**
     * Count logs by message type
     * @param messageType The type of message to count
     * @return Number of logs with the specified message type
     */
    long countByMessageType(MessageType messageType);

    /**
     * Find the most recent logs
     * @param limit Maximum number of logs to return
     * @return List of the most recent logs
     */
    @Query("SELECT l FROM Log l ORDER BY l.dateTime DESC")
    List<Log> findRecentLogs(@Param("limit") int limit);
} 