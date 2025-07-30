-- MySQL Setup for Logging Service
-- Run this script in MySQL to create the database and user

-- Create database for logging service
CREATE DATABASE IF NOT EXISTS logging_service_db;

-- Create user if not exists (same as other services)
CREATE USER IF NOT EXISTS 'vbankuser'@'localhost' IDENTIFIED BY 'vbankpass';

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'localhost';
GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'%';

-- Apply the privileges
FLUSH PRIVILEGES;

-- Show the created database
SHOW DATABASES LIKE 'logging_service_db';

-- Show user privileges
SHOW GRANTS FOR 'vbankuser'@'localhost'; 