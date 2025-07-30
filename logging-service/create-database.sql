-- Create the logging service database
-- Run this script in MySQL to create the database and grant privileges

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS logging_service_db;

-- Grant all privileges to the application user
GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'%';

-- Apply the privileges
FLUSH PRIVILEGES;

-- Show the created database
SHOW DATABASES LIKE 'logging_service_db'; 