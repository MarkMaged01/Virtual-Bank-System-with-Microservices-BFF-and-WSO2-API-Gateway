# Virtual Bank System

A comprehensive microservices-based virtual banking system built with Spring Boot, featuring user management, account management, transaction processing, and centralized logging with Kafka integration.

## 🏗️ Architecture

This project follows a microservices architecture pattern with the following components:

### Core Services

- **User Service** (`user-service`) - Port 50001
  - User registration and authentication
  - JWT token generation and validation
  - User profile management
  - Security and authorization

- **Account Service** (`account-service`) - Port 8081
  - Bank account management
  - Account creation and maintenance
  - Balance tracking

- **Transaction Service** (`transaction-service`) - Port 8085
  - Transaction processing
  - Transfer operations
  - Transaction history

- **BFF Service** (`bff-service`) - Port 5005
  - Backend for Frontend (BFF) pattern
  - API aggregation and orchestration
  - Dashboard data aggregation
  - Service integration layer

- **Logging Service** (`logging-service`)
  - Centralized logging system
  - Kafka integration for event streaming
  - Request/response logging
  - Error tracking and monitoring

### Technology Stack

- **Framework**: Spring Boot 3.5.x
- **Language**: Java 20/21
- **Database**: MySQL
- **Message Broker**: Apache Kafka
- **Security**: JWT (JSON Web Tokens)
- **Build Tool**: Maven
- **API**: RESTful APIs
- **Reactive Programming**: Spring WebFlux (BFF Service)

## 🚀 Quick Start

### Prerequisites

- Java 20 or 21
- Maven 3.6+
- MySQL 8.0+
- Apache Kafka 3.x
- PowerShell (for Windows setup scripts)

### Database Setup

1. **Install and start MySQL server**

2. **Create databases for all services**:
   ```sql
   -- Run these commands in MySQL
   CREATE DATABASE IF NOT EXISTS user_service_db;
   CREATE DATABASE IF NOT EXISTS account_service_db;
   CREATE DATABASE IF NOT EXISTS transaction_service_db;
   CREATE DATABASE IF NOT EXISTS logging_service_db;
   
   -- Create user for all services
   CREATE USER IF NOT EXISTS 'vbankuser'@'localhost' IDENTIFIED BY 'vbankpass';
   CREATE USER IF NOT EXISTS 'vbankuser'@'%' IDENTIFIED BY 'vbankpass';
   
   -- Grant privileges
   GRANT ALL PRIVILEGES ON user_service_db.* TO 'vbankuser'@'localhost';
   GRANT ALL PRIVILEGES ON account_service_db.* TO 'vbankuser'@'localhost';
   GRANT ALL PRIVILEGES ON transaction_service_db.* TO 'vbankuser'@'localhost';
   GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'localhost';
   
   GRANT ALL PRIVILEGES ON user_service_db.* TO 'vbankuser'@'%';
   GRANT ALL PRIVILEGES ON account_service_db.* TO 'vbankuser'@'%';
   GRANT ALL PRIVILEGES ON transaction_service_db.* TO 'vbankuser'@'%';
   GRANT ALL PRIVILEGES ON logging_service_db.* TO 'vbankuser'@'%';
   
   FLUSH PRIVILEGES;
   ```

### Kafka Setup

1. **Download Kafka** (if not already downloaded):
   ```powershell
   # Run from logging-service directory
   .\download-kafka.ps1
   ```

2. **Start Kafka and Zookeeper**:
   ```powershell
   # Run from logging-service directory
   .\start-logging-service-with-kafka.ps1
   ```

### Service Deployment

#### Option 1: Individual Service Startup

1. **Start User Service**:
   ```bash
   cd user-service
   mvn spring-boot:run
   ```

2. **Start Account Service**:
   ```bash
   cd account-service
   mvn spring-boot:run
   ```

3. **Start Transaction Service**:
   ```bash
   cd transaction-service
   mvn spring-boot:run
   ```

4. **Start BFF Service**:
   ```bash
   cd bff-service
   mvn spring-boot:run
   ```

5. **Start Logging Service**:
   ```bash
   cd logging-service
   mvn spring-boot:run
   ```

#### Option 2: Build All Services

```bash
# Build all services
mvn clean install -DskipTests

# Run each service from their respective directories
```

## 📋 API Documentation

### User Service Endpoints

**Base URL**: `http://localhost:50001`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | User login and JWT token generation |
| GET | `/api/users/{userId}/profile` | Get user profile |
| GET | `/api/users/{userId}/exists` | Check if user exists |
| GET | `/api/users/debug/all-users` | Get all users (debug endpoint) |

### BFF Service Endpoints

**Base URL**: `http://localhost:5005`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/bff/health` | Health check |
| GET | `/bff/dashboard/{userId}` | Get aggregated dashboard data |

### Request/Response Examples

#### User Registration
```bash
curl -X POST http://localhost:50001/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### User Login
```bash
curl -X POST http://localhost:50001/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

#### Dashboard Data
```bash
curl -X GET http://localhost:5005/bff/dashboard/{userId} \
  -H "Authorization: Bearer {jwt_token}"
```

## 🔧 Configuration

### Service Ports

| Service | Port | Description |
|---------|------|-------------|
| User Service | 50001 | User management and authentication |
| Account Service | 8081 | Account management |
| Transaction Service | 8085 | Transaction processing |
| BFF Service | 5005 | API aggregation |
| Logging Service | 8080 | Centralized logging |

### Environment Variables

Key configuration parameters can be modified in `application.properties` files:

- Database connection strings
- Kafka bootstrap servers
- JWT secret keys
- Service URLs for inter-service communication

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Encryption**: Secure password handling
- **Input Validation**: Comprehensive request validation
- **CORS Configuration**: Cross-origin resource sharing setup
- **Security Headers**: Proper HTTP security headers

## 📊 Monitoring and Logging

### Centralized Logging

The system uses Apache Kafka for centralized logging:

- **Topic**: `virtualbank-logs`
- **Partitions**: 3
- **Message Types**: Request, Response, Error
- **Logging Service**: Processes and stores all service logs

### Log Structure

```json
{
  "service": "UserService",
  "endpoint": "/api/users/register",
  "messageType": "Request",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "data": { ... }
}
```

### Testing Logging Service

```powershell
# Run from logging-service directory
.\test-logging-service.ps1
```

## 🧪 Testing

### Unit Tests

```bash
# Run tests for all services
mvn test

# Run tests for specific service
cd user-service
mvn test
```

### Integration Tests

The system includes integration tests for:
- Service-to-service communication
- Database operations
- Kafka message processing
- JWT token validation

## 🚀 Deployment

### Docker Deployment (Recommended)

1. **Build Docker images**:
   ```bash
   docker build -t virtual-bank/user-service ./user-service
   docker build -t virtual-bank/account-service ./account-service
   docker build -t virtual-bank/transaction-service ./transaction-service
   docker build -t virtual-bank/bff-service ./bff-service
   docker build -t virtual-bank/logging-service ./logging-service
   ```

2. **Run with Docker Compose**:
   ```bash
   docker-compose up -d
   ```

### Production Considerations

- Use environment variables for sensitive configuration
- Implement proper SSL/TLS certificates
- Set up monitoring and alerting
- Configure database connection pooling
- Implement rate limiting
- Set up backup and recovery procedures

## 🏗️ Project Structure

```
virtual-bank-system/
├── user-service/           # User management service
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── account-service/        # Account management service
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── transaction-service/    # Transaction processing service
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── bff-service/           # Backend for Frontend service
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── logging-service/        # Centralized logging service
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── setup-database.sql
│   ├── start-logging-service-with-kafka.ps1
│   └── pom.xml
└── README.md
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

1. Check the existing issues
2. Create a new issue with detailed description
3. Include logs and error messages
4. Specify your environment (OS, Java version, etc.)

## 🔄 Version History

- **v1.0.0** - Initial release with core banking functionality
- **v1.1.0** - Added Kafka integration for logging
- **v1.2.0** - Enhanced security with JWT tokens
- **v1.3.0** - Added BFF service for API aggregation

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [JWT.io](https://jwt.io/) - JWT token debugging
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

**Note**: This is a demonstration project. For production use, ensure proper security measures, error handling, and compliance with financial regulations. 
