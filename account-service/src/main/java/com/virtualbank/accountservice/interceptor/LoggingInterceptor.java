package com.virtualbank.accountservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualbank.accountservice.service.LoggingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.IOException;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            String requestBody = getRequestBody(request);
            if (!requestBody.isEmpty()) {
                String serviceName = "AccountService";
                String endpoint = request.getRequestURI();
                loggingService.logRequest(serviceName, endpoint, requestBody);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Log response if needed
        // Note: Response body logging is more complex and requires response wrapper
    }

    private String getRequestBody(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            // Log error but don't fail the request
        }
        return stringBuilder.toString();
    }
} 