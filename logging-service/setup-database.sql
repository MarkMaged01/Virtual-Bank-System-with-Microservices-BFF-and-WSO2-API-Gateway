-- Complete Database Setup for Logging Service
-- Run this script in MySQL as root or admin user

-- 1. Create database if not exists
CREATE DATABASE IF NOT EXISTS logging_service_db;

-- 2. Create user if not exists (same as other services)
CREATE USER IF NOT EXISTS 'vbankuser'@'localhost' IDENTIFIED BY 'vbankpass';
CREATE USER IF NOT EXISTS 'vbankuser'@'%' IDENTIFIED BY 'vbankpass';

-- 3. Grant privileges to the user
GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'localhost';
GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'%';

-- 4. Apply privilege changes
FLUSH PRIVILEGES;

-- 5. Use the database
USE logging_service_db;

-- 6. Create the dump table
CREATE TABLE IF NOT EXISTS dump (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message TEXT,
    message_type ENUM('Request', 'Response', 'Error') NOT NULL,
    date_time TIMESTAMP NOT NULL,
    INDEX idx_message_type (message_type),
    INDEX idx_date_time (date_time),
    INDEX idx_message_type_date (message_type, date_time)
);

-- 7. Verify setup
SHOW DATABASES LIKE 'logging_service_db';
SHOW TABLES IN logging_service_db;
DESCRIBE dump;
SHOW GRANTS FOR 'vbankuser'@'localhost'; 