package com.virtualbank.loggingservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualbank.loggingservice.dto.LogMessageDto;
import com.virtualbank.loggingservice.model.MessageType;
import com.virtualbank.loggingservice.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Kafka consumer for processing log messages
 * Listens to the logging topic and saves messages to the database
 */
@Component
public class LogKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LogKafkaConsumer.class);

    private final LogService logService;
    private final ObjectMapper objectMapper;

    @Autowired
    public LogKafkaConsumer(LogService logService, ObjectMapper objectMapper) {
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    /**
     * Kafka listener method that processes incoming log messages
     * @param message The JSON message from Kafka
     * @param topic The Kafka topic name
     * @param partition The partition number
     * @param offset The message offset
     * @param acknowledgment For manual acknowledgment
     */
    @KafkaListener(
            topics = "${kafka.topic.logging}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLogMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            logger.info("Received log message from topic: {}, partition: {}, offset: {}", 
                       topic, partition, offset);
            logger.debug("Raw message: {}", message);

            // Parse the JSON message
            JsonNode jsonNode = objectMapper.readTree(message);
            
            // Extract required fields from the JSON
            String messageContent = extractMessageContent(jsonNode);
            MessageType messageType = extractMessageType(jsonNode);
            LocalDateTime dateTime = extractDateTime(jsonNode);
            
            // Create LogMessageDto
            LogMessageDto logMessageDto = new LogMessageDto(messageContent, messageType, dateTime);
            
            // Save the log to database
            logService.saveLog(logMessageDto);
            
            // Manually acknowledge the message
            acknowledgment.acknowledge();
            
            logger.info("Log message processed and saved successfully");

        } catch (JsonProcessingException e) {
            logger.error("Error parsing log message JSON: {}", e.getMessage(), e);
            // Still acknowledge to avoid infinite retry
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing log message: {}", e.getMessage(), e);
            // Still acknowledge to avoid infinite retry
            acknowledgment.acknowledge();
        }
    }

    /**
     * Extract message content from JSON node
     * @param jsonNode The parsed JSON node
     * @return The message content as string
     */
    private String extractMessageContent(JsonNode jsonNode) {
        if (jsonNode.has("message") && jsonNode.get("message").isTextual()) {
            return jsonNode.get("message").asText();
        } else if (jsonNode.has("message")) {
            // If message is not a string, convert the whole node to string
            return jsonNode.get("message").toString();
        } else {
            // If no message field, use the entire JSON as message
            return jsonNode.toString();
        }
    }

    /**
     * Extract message type from JSON node
     * @param jsonNode The parsed JSON node
     * @return The MessageType enum value
     */
    private MessageType extractMessageType(JsonNode jsonNode) {
        if (jsonNode.has("messageType") && jsonNode.get("messageType").isTextual()) {
            String messageTypeStr = jsonNode.get("messageType").asText();
            return convertToMessageType(messageTypeStr);
        } else {
            // Default to Request if no messageType specified
            logger.warn("No messageType found in log message, defaulting to Request");
            return MessageType.Request;
        }
    }

    /**
     * Extract date time from JSON node
     * @param jsonNode The parsed JSON node
     * @return The LocalDateTime value
     */
    private LocalDateTime extractDateTime(JsonNode jsonNode) {
        if (jsonNode.has("dateTime") && jsonNode.get("dateTime").isTextual()) {
            String dateTimeStr = jsonNode.get("dateTime").asText();
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                logger.warn("Could not parse dateTime: {}, using current time", dateTimeStr);
                return LocalDateTime.now();
            }
        } else {
            // Use current time if no dateTime specified
            return LocalDateTime.now();
        }
    }
    
    /**
     * Convert string message type to enum
     * @param messageTypeStr The message type string
     * @return The MessageType enum value
     */
    private MessageType convertToMessageType(String messageTypeStr) {
        if (messageTypeStr == null || messageTypeStr.trim().isEmpty()) {
            return MessageType.Request; // default
        }
        
        try {
            // Try to match case-insensitive
            String normalized = messageTypeStr.trim();
            for (MessageType type : MessageType.values()) {
                if (type.name().equalsIgnoreCase(normalized)) {
                    return type;
                }
            }
            
            // If no match found, default to Request
            logger.warn("Unknown message type: {}, defaulting to Request", messageTypeStr);
            return MessageType.Request;
        } catch (Exception e) {
            logger.warn("Error parsing message type: {}, defaulting to Request", messageTypeStr);
            return MessageType.Request;
        }
    }
} 