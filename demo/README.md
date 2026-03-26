# Blood Donor & Emergency Finder Backend API

A production-ready Spring Boot backend API for managing blood donors, emergency blood requests, and donation coordination with JWT authentication and role-based access control.

## Features

### Authentication & Authorization
- Secure user registration and login with BCrypt password hashing
- JWT token-based authentication
- Role-based access control (RBAC) for three roles: Donor, Requester, Admin
- Automatic token expiration and refresh mechanism

### User Management
- User registration with role-specific details
- Email and phone number validation
- Donor profiles with blood group and donation history
- User verification and blocking capabilities for admins
- Profile management and updates

### Blood Donor Search
- Search donors by blood group
- Filter donors by city
- Check donor eligibility (minimum 90 days since last donation)
- View donor availability status
- Pagination support for large datasets

### Emergency Blood Request
- Create emergency blood requests with urgency levels (LOW, MEDIUM, HIGH, CRITICAL)
- Specify blood group, hospital, location, and units needed
- Real-time request status tracking
- Request cancellation capability

### Donor Response System
- View nearby blood requests matching donor's blood group
- Accept or reject blood requests
- OTP generation for donation verification
- Track response history
- View all active requests in user's city

### Donation Completion
- OTP verification after donation
- Record donation history with unit details
- Update donor's last donation date
- Track total donations per donor
- Maintain hospital and donation date records

### Admin Dashboard
- View system statistics (total donors, active requests, etc.)
- User management (verify, block, unblock users)
- Monitor completed donations
- Track most requested blood groups
- Paginated user management interface

## Technology Stack

- **Framework:** Spring Boot 4.0.3
- **Language:** Java 21
- **Database:** MySQL 8.0
- **Security:** Spring Security + JWT (JJWT)
- **ORM:** JPA/Hibernate
- **Build Tool:** Maven
- **Development Tools:** Lombok, Spring DevTools

## Project Structure

```
demo/
├── src/main/java/com/example/demo/
│   ├── controller/          # REST API endpoints
│   │   ├── AuthController.java
│   │   ├── DonorController.java
│   │   ├── RequesterController.java
│   │   └── AdminController.java
│   ├── service/             # Business logic layer
│   │   ├── AuthService.java
│   │   ├── DonorService.java
│   │   ├── BloodRequestService.java
│   │   ├── RequestResponseService.java
│   │   ├── DonationService.java
│   │   ├── AdminService.java
│   │   └── AuthorizationService.java
│   ├── repository/          # Data access layer
│   │   ├── UserRepository.java
│   │   ├── DonorDetailsRepository.java
│   │   ├── BloodRequestRepository.java
│   │   ├── RequestResponseRepository.java
│   │   └── DonationHistoryRepository.java
│   ├── entity/              # JPA entities
│   │   ├── User.java
│   │   ├── DonorDetails.java
│   │   ├── BloodRequest.java
│   │   ├── RequestResponse.java
│   │   └── DonationHistory.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── UserRegistrationRequest.java
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── DonorDetailsResponse.java
│   │   ├── BloodRequestRequest.java
│   │   ├── BloodRequestResponse.java
│   │   ├── RequestResponseDTO.java
│   │   ├── DonationCompletionRequest.java
│   │   └── AdminDashboardResponse.java
│   ├── security/            # JWT and Security
│   │   ├── JwtTokenProvider.java
│   │   └── JwtAuthenticationFilter.java
│   ├── config/              # Spring Configuration
│   │   └── SecurityConfig.java
│   ├── exception/           # Exception Handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   ├── UnauthorizedException.java
│   │   └── ErrorResponse.java
│   └── DemoApplication.java # Main Application Class
├── src/main/resources/
│   ├── application.properties
│   └── schema.sql           # Database schema
├── pom.xml
└── API_DOCUMENTATION.md     # API endpoint documentation
```

## Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- Git (optional)

## Installation & Setup

### 1. Clone the Repository
```bash
cd "Blood donor backend"
```

### 2. Configure Database

Create a MySQL database:
```sql
CREATE DATABASE blood_donor_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blood_donor_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Install Dependencies
```bash
mvn clean install
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 5. Initialize Database Schema (Optional)
Execute the SQL script from `src/main/resources/schema.sql` in MySQL:
```bash
mysql -u root -p blood_donor_db < src/main/resources/schema.sql
```

## Configuration

### JWT Secret Key
Update the `app.jwtSecret` in `application.properties`. Use a strong, long key (at least 256 bits):

```properties
app.jwtSecret=YourVeryLongSecretKeyFor256BitsAtLeast...
app.jwtExpirationInMs=86400000  # 24 hours
```

### Database Settings
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blood_donor_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

## API Endpoints Summary

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Donor Operations
- `GET /api/donors/search` - Search donors by blood group and city
- `GET /api/donors/{donorId}` - Get donor details
- `PUT /api/donors/{donorId}/availability` - Update availability
- `GET /api/donors/{donorId}/responses` - View response history
- `POST /api/donors/respond` - Respond to blood request

### Requester Operations
- `POST /api/requesters/{requesterId}/requests` - Create blood request
- `GET /api/requesters/{requesterId}/requests` - View my requests
- `GET /api/requesters/requests/active` - View active requests
- `GET /api/requesters/requests/search` - Search requests
- `GET /api/requesters/requests/{requestId}/responses` - View donor responses
- `POST /api/requesters/{requesterId}/donations/verify` - Verify OTP and record donation
- `DELETE /api/requesters/{requesterId}/requests/{requestId}` - Cancel request

### Admin Operations
- `GET /api/admin/dashboard` - View statistics
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{userId}/verify` - Verify user
- `PUT /api/admin/users/{userId}/block` - Block user
- `PUT /api/admin/users/{userId}/unblock` - Unblock user

## Database Schema

### Users Table
- User credentials and basic information
- Role-based classification (DONOR, REQUESTER, ADMIN)
- Verification and blocking status

### DonorDetails Table
- Blood group and donation history
- Eligibility tracking (90-day rule)
- Availability status

### BloodRequests Table
- Emergency request details
- Status tracking (PENDING, MATCHED, COMPLETED)
- Urgency levels

### RequestResponses Table
- Donor responses to requests
- OTP verification for donations
- Units provided tracking

### DonationHistory Table
- Complete donation records
- Hospital and date details
- Donation amount tracking

## Security Features

### Password Security
- BCrypt hashing algorithm for password storage
- Minimum 8 characters with uppercase, lowercase, digit, and special character
- No plain text passwords stored in database

### JWT Authentication
- Token expires after 24 hours (configurable)
- Stateless authentication (no session storage)
- Bearer token scheme in Authorization header

### Authorization
- Method-level security with `@PreAuthorize` annotations
- Role-based access control via Spring Security
- Request verification to ensure user owns their data

### Input Validation
- Email format validation
- Phone number format validation (10 digits)
- Password strength validation
- DTO-based validation using Jakarta Validation

## Error Handling

Global exception handling with consistent error response format:
```json
{
  "message": "Error description",
  "status": 400,
  "timestamp": "2024-03-12T10:35:00",
  "validationErrors": {...}
}
```

## Performance Optimization

- Database indexing on frequently queried columns
- Lazy loading for entity relationships
- Pagination for list endpoints
- Prepared statements via Spring Data JPA
- Connection pooling

## Best Practices Implemented

1. **Clean Architecture:** Separation of concerns with layered architecture
2. **RESTful Design:** Proper HTTP methods and status codes
3. **Data Validation:** Input validation at DTO and service levels
4. **Error Handling:** Global exception handler with meaningful error messages
5. **Security:** JWT authentication, password hashing, role-based access control
6. **Documentation:** Comprehensive API documentation and code comments
7. **Database Design:** Normalized schema with proper relationships and indexes
8. **Pagination:** Support for large datasets with pagination

## Running Tests

```bash
mvn test
```

## Building for Production

```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Deployment

### Using Docker
Create a Dockerfile:
```dockerfile
FROM openjdk:21-slim
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t blood-donor-api .
docker run -p 8080:8080 blood-donor-api
```

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://db-server:3306/blood_donor_db
export SPRING_DATASOURCE_USERNAME=user
export SPRING_DATASOURCE_PASSWORD=password
export APP_JWTSECRET=your-secret-key
```

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify credentials in `application.properties`

### JWT Token Errors
- Ensure token is included in Authorization header: `Bearer <token>`
- Check token expiration time
- Verify JWT secret matches configuration

### Port Already in Use
```bash
# Change port in application.properties
server.port=8081
```

## Future Enhancements

1. SMS/Email notifications for blood requests
2. Integration with payment gateway for donor incentives
3. Mobile app integration
4. Real-time WebSocket notifications
5. Blood bank inventory management
6. Advanced analytics and reporting
7. Multi-language support
8. Two-factor authentication (2FA)

## Contributing

1. Follow Java naming conventions (camelCase for variables, PascalCase for classes)
2. Write meaningful commit messages
3. Add unit tests for new features
4. Update API documentation for new endpoints

## License

This project is proprietary and confidential.

## Support

For issues and questions, contact the development team.

---

**Version:** 1.0.0  
**Last Updated:** March 12, 2024  
**Status:** Production Ready
