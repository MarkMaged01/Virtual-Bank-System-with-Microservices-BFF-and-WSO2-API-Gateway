package com.Ejada.BFF.Interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String LOGGING_TOPIC = "bff.logs";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        String requestBody = new String(wrappedRequest.getContentAsByteArray());
        String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header for request {}: {}", request.getRequestURI(), authHeader);

        if (!requestBody.isEmpty()) {
            try {
                Map<String, Object> logData = new HashMap<>();
                logData.put("message", requestBody);
                logData.put("messageType", "Request");
                logData.put("dateTime", LocalDateTime.now().toString());
                logData.put("uri", request.getRequestURI());
                String logJson = objectMapper.writeValueAsString(logData);
                kafkaTemplate.send(LOGGING_TOPIC, logJson);
                logger.debug("Logged request: {}", logJson);
            } catch (Exception e) {
                logger.error("Failed to log request: {}", e.getMessage());
            }
        }

        return true;
    }
}
