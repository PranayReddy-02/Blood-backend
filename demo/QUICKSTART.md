# Blood Donor API - Quick Start Guide

## 5-Minute Setup

### Prerequisites Check

```bash
# Check Java
java -version  # Should be 21+

# Check Maven
mvn -version   # Should be 3.6+

# Check MySQL
mysql --version  # Should be 8.0+
```

### Step 1: Build the Project (2 minutes)

```bash
cd "Blood donor backend/demo"
mvn clean install
```

### Step 2: Create Database

```bash
# Create database
mysql -u root -p < src/main/resources/schema.sql
```

### Step 3: Start Application

```bash
mvn spring-boot:run
```

**✓ Application now running at `http://localhost:8080`**

---

## First API Call (Register a Donor)

### Using cURL

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Donor",
    "email": "donor@example.com",
    "password": "Test@1234",
    "phoneNumber": "9876543210",
    "city": "Mumbai",
    "role": "DONOR",
    "bloodGroup": "O+"
  }'
```

### Response

```json
{
  "id": 1,
  "name": "John Donor",
  "email": "donor@example.com",
  "phoneNumber": "9876543210",
  "city": "Mumbai",
  "role": "DONOR",
  "isVerified": false,
  "isBlocked": false,
  "createdAt": "2024-03-12T10:30:00"
}
```

---

## Login & Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "donor@example.com",
    "password": "Test@1234"
  }'
```

**Save the token from response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400000
}
```

---

## Using the Token (Protected Endpoint)

### Search Donors

```bash
curl -X GET "http://localhost:8080/api/donors/search?bloodGroup=O%2B&page=0&size=20"
```

### Update Donor Availability (Requires Auth)

```bash
curl -X PUT "http://localhost:8080/api/donors/1/availability?isAvailable=true" \
  -H "Authorization: Bearer <your_token>"
```

---

## Complete User Flow Example

### 1. Register Requester

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Requester",
    "email": "requester@example.com",
    "password": "Test@1234",
    "phoneNumber": "9876543211",
    "city": "Mumbai",
    "role": "REQUESTER"
  }'
```

**Save response ID: `2`**

### 2. Login as Requester

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "requester@example.com",
    "password": "Test@1234"
  }' | jq -r '.token')

echo $TOKEN
```

### 3. Create Blood Request

```bash
curl -X POST http://localhost:8080/api/requesters/2/requests \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bloodGroup": "O+",
    "hospitalName": "Apollo Hospital",
    "location": "Bandra, Mumbai",
    "urgencyLevel": "HIGH",
    "unitsRequired": 2,
    "notes": "Emergency transfusion needed"
  }'
```

**Save request ID: `1`**

### 4. View Active Requests

```bash
curl -X GET "http://localhost:8080/api/requesters/requests/active?page=0&size=20"
```

### 5. Donor Responds to Request

```bash
DONOR_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "donor@example.com",
    "password": "Test@1234"
  }' | jq -r '.token')

curl -X POST "http://localhost:8080/api/donors/respond?requestId=1&donorId=1&action=ACCEPT" \
  -H "Authorization: Bearer $DONOR_TOKEN"
```

### 6. View OTP (From Response)

The response will include the OTP. In production, this would be sent via email/SMS.

```json
{
  "otp": "123456",
  "status": "ACCEPTED"
}
```

### 7. Verify OTP and Complete Donation

```bash
curl -X POST http://localhost:8080/api/requesters/2/donations/verify \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "requestResponseId": 1,
    "otp": "123456",
    "unitsProvided": 2,
    "notes": "Donation completed successfully"
  }'
```

---

## Admin Operations

### 1. Register Admin User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@example.com",
    "password": "Admin@1234",
    "phoneNumber": "9876543212",
    "city": "Mumbai",
    "role": "ADMIN"
  }'
```

### 2. Login as Admin

```bash
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "Admin@1234"
  }' | jq -r '.token')
```

### 3. View Dashboard

```bash
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 4. List All Users

```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 5. Verify User

```bash
curl -X PUT "http://localhost:8080/api/admin/users/1/verify" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 6. Block User

```bash
curl -X PUT "http://localhost:8080/api/admin/users/1/block" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## Testing Endpoints with Postman

### 1. Import Collection

Create a new Postman collection with these requests:

**Auth Endpoints:**
- POST `/api/auth/register`
- POST `/api/auth/login`

**Donor Endpoints:**
- GET `/api/donors/search`
- GET `/api/donors/{id}`
- PUT `/api/donors/{id}/availability`
- GET `/api/donors/{id}/responses`
- POST `/api/donors/respond`

**Requester Endpoints:**
- POST `/api/requesters/{id}/requests`
- GET `/api/requesters/{id}/requests`
- GET `/api/requesters/requests/active`
- GET `/api/requesters/requests/search`
- POST `/api/requesters/{id}/donations/verify`

**Admin Endpoints:**
- GET `/api/admin/dashboard`
- GET `/api/admin/users`
- PUT `/api/admin/users/{id}/verify`
- PUT `/api/admin/users/{id}/block`

### 2. Set Environment Variables

```
base_url: http://localhost:8080
donor_token: {{save from login response}}
requester_token: {{save from login response}}
admin_token: {{save from login response}}
```

### 3. Use Bearer Token

In Postman Authorization tab, select "Bearer Token" and use:
```
{{donor_token}}
```

---

## Common Error Messages & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `Port 8080 already in use` | Another app using port | Kill process: `lsof -i :8080` |
| `Connection refused - MySQL` | MySQL not running | Start: `mysql.server start` |
| `Access denied for user` | Wrong password | Check `.properties` file |
| `Email already registered` | Duplicate email | Use unique email |
| `Invalid password` | Weak password | Min 8 chars: uppercase, lowercase, digit, special |
| `401 Unauthorized` | Missing/invalid token | Get token from login first |
| `403 Forbidden` | Wrong role | Ensure correct role for endpoint |

---

## Development Workflow

### 1. Make Code Changes

Edit service/controller:
```java
@Service
public class NewService {
    // Your code
}
```

### 2. Rebuild (Optional)

The app auto-reloads with DevTools:
```bash
mvn spring-boot:run
```

### 3. Test Changes

```bash
curl -X GET http://localhost:8080/api/endpoint
```

### 4. View Logs

Check terminal output for DEBUG logs:
```
2024-03-12 10:30:45 [main] DEBUG c.example.demo.service.AuthService
```

---

## Database Inspection

### View All Users

```sql
mysql -u root -p blood_donor_db
SELECT * FROM users;
```

### View Donor Details

```sql
SELECT u.*, d.* 
FROM users u 
JOIN donor_details d ON u.id = d.user_id;
```

### View Active Requests

```sql
SELECT * FROM blood_requests 
WHERE status IN ('PENDING', 'MATCHED');
```

### Clear Test Data

```sql
DELETE FROM donation_history;
DELETE FROM request_responses;
DELETE FROM blood_requests;
DELETE FROM donor_details;
DELETE FROM users;
```

---

## IDE Setup (IntelliJ/VS Code)

### IntelliJ IDEA

1. Open project: File → Open → Select demo folder
2. Configure JDK: File → Project Structure → SDK → JDK 21
3. Enable annotation processing: Settings → Build → Compiler → Annotation Processors
4. Maven: View → Tool Windows → Maven → Enable auto-reload
5. Run: Run → Run 'DemoApplication'

### VS Code

1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - REST Client

2. Create `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.example.demo.DemoApplication",
      "args": "",
      "cwd": "${workspaceFolder}"
    }
  ]
}
```

3. Press F5 to start debugging

---

## Building for Production

### Create JAR

```bash
mvn clean package -DskipTests
```

**Output:** `target/demo-0.0.1-SNAPSHOT.jar`

### Run JAR

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### With Custom Config

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://prod-db:3306/blood_donor \
  --app.jwtSecret=your-secret-key
```

---

## Useful Commands

```bash
# Build only
mvn clean compile

# Run tests
mvn test

# Check formatting
mvn checkstyle:check

# Generate dependency tree
mvn dependency:tree

# Update dependencies
mvn versions:update-properties

# Create executable JAR
mvn clean package

# Skip tests during build
mvn clean install -DskipTests

# Enable debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"

# Run with profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

---

## Next Steps

1. **Read Full Documentation:**
   - [API Documentation](API_DOCUMENTATION.md)
   - [Architecture](ARCHITECTURE.md)
   - [Deployment Guide](DEPLOYMENT_GUIDE.md)

2. **Explore Codebase:**
   - Start with controllers in `src/main/java/com/example/demo/controller`
   - Study services for business logic
   - Review entities for database structure

3. **Implement Features:**
   - Add email notifications
   - Implement SMS alerts
   - Create mobile API endpoints

4. **Deploy:**
   - Follow [Deployment Guide](DEPLOYMENT_GUIDE.md)
   - Set up CI/CD pipeline
   - Configure monitoring

---

## Support & Resources

- **Spring Boot Docs:** https://spring.io/projects/spring-boot
- **JPA Documentation:** https://docs.jboss.org/hibernate/orm/latest/userguide/
- **JWT Guide:** https://jwt.io
- **REST Best Practices:** https://restfulapi.net

---

**Happy Coding! 🚀**

Last Updated: March 12, 2024
