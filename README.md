# Spring Boot Authentication Service

This is the authentication microservice for the PubGenAI paper management system, built with Spring Boot 3.2 and JWT authentication.

## 🏗️ Architecture

- **Spring Boot 3.2** - Main framework
- **Spring Security** - Authentication and authorization
- **JWT** - Token-based authentication
- **H2 Database** - In-memory database for development
- **Google OAuth 2.0** - SSO integration
- **BCrypt** - Password encryption

## 🚀 Features

- User registration and login
- Google SSO integration
- JWT token generation and validation
- Role-based access control
- Token refresh mechanism
- CORS configuration

## 📁 Project Structure

```
src/main/java/com/pubgenai/
├── controller/           # REST controllers
│   └── AuthController.java
├── service/             # Business logic
│   ├── AuthService.java
│   ├── GoogleAuthService.java
│   └── UserDetailsServiceImpl.java
├── repository/          # Data access layer
│   └── UserRepository.java
├── model/              # Entity models
│   ├── User.java
│   └── Role.java
├── dto/                # Data transfer objects
│   ├── AuthResponse.java
│   ├── UserDto.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── GoogleSSORequest.java
│   ├── RefreshTokenRequest.java
│   └── ErrorResponse.java
└── security/           # Security configuration
    ├── JwtUtil.java
    ├── JwtAuthenticationFilter.java
    └── SecurityConfig.java
```

## 🛠️ Setup and Installation

### Prerequisites
- Java 17+
- Maven 3.6+

### 1. Clone and Navigate
```bash
cd backend/spring-boot
```

### 2. Configure Application
Update `src/main/resources/application.properties`:

```properties
# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400

# Google OAuth Configuration
google.client-id=your-google-client-id

# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=password
```

### 3. Run the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

## 🔐 Security Configuration

### JWT Configuration
- **Secret Key**: Configured in `application.properties`
- **Expiration**: 24 hours (86400 seconds)
- **Algorithm**: HS256

### CORS Configuration
- **Allowed Origins**: `http://localhost:3000`
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: All headers
- **Credentials**: Enabled

### Security Endpoints
- `/api/auth/**` - Public endpoints
- `/api/domains` - Public
- `/api/publications` - Public
- `/api/statistics` - Public
- All other endpoints require authentication

## 📚 API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "USER"
}
```

#### Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### Google SSO
```http
POST /api/auth/google-sso
Content-Type: application/json

{
  "credential": "google-jwt-token"
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refresh_token": "refresh-token-here"
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <jwt-token>
```

### Response Format

#### Successful Authentication
```json
{
  "access_token": "jwt-access-token",
  "refresh_token": "jwt-refresh-token",
  "user": {
    "id": 1,
    "email": "john@example.com",
    "name": "John Doe",
    "role": "USER"
  }
}
```

#### Error Response
```json
{
  "detail": "Error message here"
}
```

## 🔧 Configuration

### Environment Variables
```bash
# JWT Configuration
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400

# Google OAuth
GOOGLE_CLIENT_ID=your-google-client-id

# Database
SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=password
```

### Google OAuth Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable Google+ API
4. Create OAuth 2.0 credentials
5. Add authorized origins:
   - `http://localhost:3000`
   - `http://localhost:8080`
6. Copy Client ID to configuration

## 🗄️ Database Schema

### User Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    google_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Roles
- `USER` - Basic user, can submit papers
- `EDITOR` - Can manage papers and access editor directory
- `REVIEWER` - Can review papers and access reviewer directory
- `PUBLISHER` - Premium access to AI features

## 🔒 Security Features

### Password Security
- BCrypt encryption
- Salt generation
- Configurable strength

### JWT Security
- HMAC SHA-256 signing
- Configurable expiration
- Refresh token mechanism
- Token validation

### CORS Security
- Origin validation
- Method restrictions
- Header validation
- Credential handling

## 🧪 Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests
```bash
./mvnw integration-test
```

### Manual Testing
Use the H2 console at `http://localhost:8080/h2-console` to inspect the database.

## 🚀 Deployment

### Production Configuration
1. Update `application.properties` for production
2. Configure production database
3. Set up SSL certificates
4. Configure environment variables
5. Deploy to your hosting service

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/paper-management-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔍 Monitoring

### Health Check
```http
GET /actuator/health
```

### Metrics
```http
GET /actuator/metrics
```

## 🆘 Troubleshooting

### Common Issues

1. **CORS Errors**
   - Check CORS configuration in `SecurityConfig.java`
   - Verify allowed origins

2. **JWT Token Issues**
   - Verify JWT secret configuration
   - Check token expiration

3. **Google OAuth Issues**
   - Verify Google Client ID
   - Check authorized origins

4. **Database Issues**
   - Check H2 console access
   - Verify database configuration

## 📞 Support

For issues and questions:
- Check the logs for error messages
- Verify configuration settings
- Test with Postman or similar tools
- Contact the development team

---

**Spring Boot Authentication Service - Part of PubGenAI System**
