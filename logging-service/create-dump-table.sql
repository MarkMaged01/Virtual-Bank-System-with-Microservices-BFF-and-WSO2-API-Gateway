-- Create the dump table for logging service
-- Run this script in MySQL to create the table manually

USE logging_service_db;

-- Create the dump table
CREATE TABLE IF NOT EXISTS dump (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT,
    message_type ENUM('Request', 'Response', 'Error') NOT NULL,
    date_time TIMESTAMP NOT NULL,
    INDEX idx_message_type (message_type),
    INDEX idx_date_time (date_time),
    INDEX idx_message_type_date (message_type, date_time)
);

-- Verify the table was created
SHOW TABLES LIKE 'dump';
DESCRIBE dump; 