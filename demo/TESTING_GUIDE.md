# Blood Donor API - Testing Guide

## Unit Testing Guide

### Authentication Service Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private AuthService authService;
    
    @MockBean
    private UserRepository userRepository;
    
    @Test
    public void testUserRegistration_Success() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
            .name("John Doe")
            .email("john@example.com")
            .password("SecurePass@123")
            .phoneNumber("9876543210")
            .city("Mumbai")
            .role("DONOR")
            .bloodGroup("O+")
            .build();
        
        UserResponse response = authService.register(request);
        
        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
    }
    
    @Test
    public void testLogin_Success() {
        LoginRequest request = LoginRequest.builder()
            .email("john@example.com")
            .password("SecurePass@123")
            .build();
        
        LoginResponse response = authService.login(request);
        
        assertNotNull(response.getToken());
        assertEquals("DONOR", response.getRole());
    }
}
```

### Donor Service Tests

```java
@SpringBootTest
public class DonorServiceTest {
    
    @Autowired
    private DonorService donorService;
    
    @MockBean
    private DonorDetailsRepository donorDetailsRepository;
    
    @Test
    public void testSearchDonors_ByBloodGroup() {
        Pageable pageable = PageRequest.of(0, 20);
        
        Page<DonorDetailsResponse> result = donorService
            .searchDonorsByBloodGroup("O+", pageable);
        
        assertNotNull(result);
    }
}
```

### Blood Request Service Tests

```java
@SpringBootTest
public class BloodRequestServiceTest {
    
    @Autowired
    private BloodRequestService bloodRequestService;
    
    @MockBean
    private BloodRequestRepository bloodRequestRepository;
    
    @Test
    public void testCreateBloodRequest_Success() {
        BloodRequestRequest request = BloodRequestRequest.builder()
            .bloodGroup("O+")
            .hospitalName("Apollo Hospital")
            .location("Bandra, Mumbai")
            .urgencyLevel("HIGH")
            .unitsRequired(2)
            .notes("Emergency")
            .build();
        
        // Test logic
    }
}
```

## Integration Testing Guide

### Auth API Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testRegisterEndpoint() throws Exception {
        String requestBody = """
            {
                "name": "John Doe",
                "email": "john@test.com",
                "password": "SecurePass@123",
                "phoneNumber": "9876543210",
                "city": "Mumbai",
                "role": "DONOR",
                "bloodGroup": "O+"
            }
            """;
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("john@test.com"));
    }
    
    @Test
    public void testLoginEndpoint() throws Exception {
        String requestBody = """
            {
                "email": "john@test.com",
                "password": "SecurePass@123"
            }
            """;
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }
}
```

### Donor API Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class DonorControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testSearchDonorsEndpoint() throws Exception {
        mockMvc.perform(get("/api/donors/search")
                .param("bloodGroup", "O+")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
    
    @Test
    public void testGetDonorDetailsEndpoint() throws Exception {
        mockMvc.perform(get("/api/donors/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

## API Testing with Postman/cURL

### 1. Register a Donor

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Donor",
    "email": "john.donor@example.com",
    "password": "SecurePass@123",
    "phoneNumber": "9876543210",
    "city": "Mumbai",
    "role": "DONOR",
    "bloodGroup": "O+"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.donor@example.com",
    "password": "SecurePass@123"
  }'
```

### 3. Search Donors

```bash
curl -X GET "http://localhost:8080/api/donors/search?bloodGroup=O%2B&page=0&size=20"
```

### 4. Get Donor Details

```bash
curl -X GET http://localhost:8080/api/donors/1
```

### 5. Create Blood Request (Authenticated)

```bash
curl -X POST http://localhost:8080/api/requesters/2/requests \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "bloodGroup": "O+",
    "hospitalName": "Apollo Hospital",
    "location": "Bandra, Mumbai",
    "urgencyLevel": "HIGH",
    "unitsRequired": 2,
    "notes": "Emergency transfusion"
  }'
```

### 6. Respond to Request (Donor)

```bash
curl -X POST "http://localhost:8080/api/donors/respond?requestId=1&donorId=1&action=ACCEPT"
```

### 7. Verify OTP (Requester)

```bash
curl -X POST http://localhost:8080/api/requesters/2/donations/verify \
  -H "Authorization: Bearer <jwt_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "requestResponseId": 1,
    "otp": "123456",
    "unitsProvided": 2,
    "notes": "Donation completed"
  }'
```

### 8. Admin Dashboard

```bash
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer <admin_jwt_token>"
```

## Test Data Setup

### Insert Test Donor

```sql
INSERT INTO users (email, password, name, phone_number, city, role, is_verified, is_blocked)
VALUES ('donor1@test.com', '$2a$10$...hashed_password...', 'Test Donor', '9876543210', 'Mumbai', 'DONOR', true, false);

INSERT INTO donor_details (user_id, blood_group, is_available, total_donations)
VALUES (1, 'O_POSITIVE', true, 0);
```

### Insert Test Requester

```sql
INSERT INTO users (email, password, name, phone_number, city, role, is_verified, is_blocked)
VALUES ('requester1@test.com', '$2a$10$...hashed_password...', 'Test Requester', '9876543211', 'Mumbai', 'REQUESTER', true, false);
```

## Performance Testing

### Load Testing with Apache JMeter

1. Create a test plan
2. Add Thread Group with 100 users
3. Add HTTP Requests for:
   - Search Donors: GET /api/donors/search
   - Create Request: POST /api/requesters/{id}/requests
   - Get Active Requests: GET /api/requesters/requests/active
4. Run and analyze results

## Manual Testing Checklist

### Authentication
- [ ] User registration with valid data
- [ ] User registration with duplicate email (should fail)
- [ ] User registration with weak password (should fail)
- [ ] Valid login attempt
- [ ] Invalid password login attempt
- [ ] Non-existent user login attempt

### Donor Operations
- [ ] Search donors by blood group
- [ ] Search donors by city
- [ ] Search donors by blood group and city
- [ ] View donor eligibility status
- [ ] Update donor availability
- [ ] View donor response history
- [ ] Respond to blood request

### Requester Operations
- [ ] Create blood request with valid data
- [ ] View personal blood requests
- [ ] Search blood requests
- [ ] Filter requests by urgency
- [ ] View donor responses to request
- [ ] Verify OTP after donation
- [ ] Cancel blood request

### Admin Operations
- [ ] View dashboard statistics
- [ ] List all users with pagination
- [ ] Verify user account
- [ ] Block user
- [ ] Unblock user

## Continuous Integration

### GitHub Actions Workflow

```yaml
name: Blood Donor API CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        java-version: '21'
    - name: Build with Maven
      run: mvn clean build
    - name: Run tests
      run: mvn test
    - name: Generate coverage report
      run: mvn jacoco:report
```

## Coverage Goals

- **Unit Tests:** 80% coverage
- **Integration Tests:** 60% coverage
- **API Tests:** 100% endpoint coverage

## Monitoring & Logging

Enable logging in `application.properties`:

```properties
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate=INFO
```

Monitor application metrics:
- Response times
- Error rates
- Database query performance
- JWT token validation

