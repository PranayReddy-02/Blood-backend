# Blood Donor & Emergency Finder API Documentation

## Base URL
```
http://localhost:8080/api
```

---

## Authentication Endpoints

### 1. User Registration
**POST** `/auth/register`

**Description:** Register a new user (Donor, Requester, or Admin)

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass@123",
  "phoneNumber": "9876543210",
  "city": "Mumbai",
  "role": "DONOR",
  "bloodGroup": "O+"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "city": "Mumbai",
  "role": "DONOR",
  "isVerified": false,
  "isBlocked": false,
  "createdAt": "2024-03-12T10:30:00"
}
```

---

### 2. User Login
**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass@123"
}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "email": "john@example.com",
  "name": "John Doe",
  "role": "DONOR",
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiRE9OT1IiLCJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNzEwMzE1ODAwLCJleHAiOjE3MTAzNDAxNjB9.abcdef...",
  "expiresIn": 86400000
}
```

---

## Donor Endpoints

### 3. Search Donors by Blood Group
**GET** `/donors/search?bloodGroup=O+&page=0&size=20`

**Description:** Search available donors by blood group and optionally by city

**Query Parameters:**
- `bloodGroup` (required): Blood group (e.g., O+, A-, B+, AB-)
- `city` (optional): Filter by city
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "phoneNumber": "9876543210",
      "city": "Mumbai",
      "bloodGroup": "O+",
      "lastDonationDate": "2024-01-15",
      "isAvailable": true,
      "totalDonations": 5,
      "isEligible": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "size": 20
}
```

---

### 4. Get Donor Details
**GET** `/donors/{donorId}`

**Description:** Get detailed information about a specific donor

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "city": "Mumbai",
  "bloodGroup": "O+",
  "lastDonationDate": "2024-01-15",
  "isAvailable": true,
  "totalDonations": 5,
  "isEligible": true
}
```

---

### 5. Update Donor Availability
**PUT** `/donors/{donorId}/availability?isAvailable=true`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Description:** Update donor's availability status

**Response (204 No Content)**

---

### 6. Get Donor Responses to Blood Requests
**GET** `/donors/{donorId}/responses?page=0&size=20`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Description:** Get all blood request responses from a donor

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "requestId": 5,
      "donorId": 1,
      "donorName": "John Doe",
      "status": "ACCEPTED",
      "otp": "123456",
      "otpVerified": true,
      "unitsProvided": 2,
      "responseDate": "2024-03-12T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "size": 20
}
```

---

## Requester Endpoints

### 7. Create Blood Request
**POST** `/requesters/{requesterId}/requests`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "bloodGroup": "O+",
  "hospitalName": "Apollo Hospital",
  "location": "Bandra, Mumbai",
  "urgencyLevel": "HIGH",
  "unitsRequired": 2,
  "notes": "Emergency blood transfusion needed"
}
```

**Response (201 Created):**
```json
{
  "id": 5,
  "requesterName": "Jane Smith",
  "bloodGroup": "O+",
  "hospitalName": "Apollo Hospital",
  "location": "Bandra, Mumbai",
  "urgencyLevel": "HIGH",
  "unitsRequired": 2,
  "status": "PENDING",
  "notes": "Emergency blood transfusion needed",
  "createdAt": "2024-03-12T10:30:00",
  "updatedAt": "2024-03-12T10:30:00"
}
```

---

### 8. Get My Blood Requests
**GET** `/requesters/{requesterId}/requests`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
[
  {
    "id": 5,
    "requesterName": "Jane Smith",
    "bloodGroup": "O+",
    "hospitalName": "Apollo Hospital",
    "location": "Bandra, Mumbai",
    "urgencyLevel": "HIGH",
    "unitsRequired": 2,
    "status": "PENDING",
    "notes": "Emergency blood transfusion needed",
    "createdAt": "2024-03-12T10:30:00",
    "updatedAt": "2024-03-12T10:30:00"
  }
]
```

---

### 9. Get Active Blood Requests
**GET** `/requesters/requests/active?page=0&size=20`

**Description:** Get all active blood requests

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 5,
      "requesterName": "Jane Smith",
      "bloodGroup": "O+",
      "hospitalName": "Apollo Hospital",
      "location": "Bandra, Mumbai",
      "urgencyLevel": "HIGH",
      "unitsRequired": 2,
      "status": "PENDING",
      "notes": "Emergency blood transfusion needed",
      "createdAt": "2024-03-12T10:30:00",
      "updatedAt": "2024-03-12T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "size": 20
}
```

---

### 10. Search Blood Requests
**GET** `/requesters/requests/search?bloodGroup=O+&location=Bandra&page=0&size=20`

**Description:** Search blood requests by blood group and location

**Query Parameters:**
- `bloodGroup` (required): Blood group
- `location` (optional): Location filter
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size

**Response:** Same as Get Active Blood Requests

---

### 11. Get Blood Request Details
**GET** `/requesters/requests/{requestId}`

**Response (200 OK):** Same blood request object as above

---

### 12. Get Responses to a Blood Request
**GET** `/requesters/requests/{requestId}/responses?page=0&size=20`

**Description:** Get all donor responses to a specific blood request

**Response (200 OK):** Returns paginated list of RequestResponseDTO objects

---

### 13. Donor Response Endpoint (from DonorController)
**POST** `/donors/respond`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "requestId": 5,
  "donorId": 1,
  "action": "ACCEPT"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "requestId": 5,
  "donorId": 1,
  "donorName": "John Doe",
  "status": "ACCEPTED",
  "otp": "123456",
  "otpVerified": false,
  "unitsProvided": null,
  "responseDate": "2024-03-12T10:35:00"
}
```

---

### 14. Verify Donation (OTP)
**POST** `/requesters/{requesterId}/donations/verify`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "requestResponseId": 1,
  "otp": "123456",
  "unitsProvided": 2,
  "notes": "Donation completed successfully"
}
```

**Response (204 No Content)**

---

### 15. Cancel Blood Request
**DELETE** `/requesters/{requesterId}/requests/{requestId}`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (204 No Content)**

---

## Admin Endpoints

### 16. Get Dashboard Statistics
**GET** `/admin/dashboard`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "totalDonors": 100,
  "totalRequesters": 50,
  "activeDonors": 85,
  "activeRequests": 20,
  "completedDonations": 300,
  "mostRequestedBloodGroup": "O+",
  "totalVerifiedUsers": 120,
  "totalBlockedUsers": 5
}
```

---

### 17. Get All Users
**GET** `/admin/users?page=0&size=20`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "phoneNumber": "9876543210",
      "city": "Mumbai",
      "role": "DONOR",
      "isVerified": true,
      "isBlocked": false,
      "createdAt": "2024-03-12T10:30:00"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "size": 20
}
```

---

### 18. Verify User
**PUT** `/admin/users/{userId}/verify`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "city": "Mumbai",
  "role": "DONOR",
  "isVerified": true,
  "isBlocked": false,
  "createdAt": "2024-03-12T10:30:00"
}
```

---

### 19. Block User
**PUT** `/admin/users/{userId}/block`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "city": "Mumbai",
  "role": "DONOR",
  "isVerified": true,
  "isBlocked": true,
  "createdAt": "2024-03-12T10:30:00"
}
```

---

### 20. Unblock User
**PUT** `/admin/users/{userId}/unblock`

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "city": "Mumbai",
  "role": "DONOR",
  "isVerified": true,
  "isBlocked": false,
  "createdAt": "2024-03-12T10:30:00"
}
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "message": "Error description",
  "status": 400,
  "timestamp": "2024-03-12T10:35:00",
  "validationErrors": {
    "email": "Email should be valid",
    "password": "Password must be at least 8 characters..."
  }
}
```

### Common Status Codes:
- `200 OK`: Successful GET/PUT request
- `201 Created`: Successful POST request
- `204 No Content`: Successful DELETE/PUT request with no response body
- `400 Bad Request`: Validation error or invalid input
- `401 Unauthorized`: Missing or invalid authentication token
- `403 Forbidden`: User doesn't have permission for this resource
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

## Authentication Header

All authenticated endpoints require:
```
Authorization: Bearer <jwt_token>
```

The JWT token is obtained from the login endpoint and should be included in the Authorization header for all protected routes.

---

## Notes

1. **Blood Groups:** O+, O-, A+, A-, B+, B-, AB+, AB-
2. **Urgency Levels:** LOW, MEDIUM, HIGH, CRITICAL
3. **Request Status:** PENDING, MATCHED, IN_PROGRESS, COMPLETED, CANCELLED
4. **Response Status:** PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
5. **Donor Eligibility:** Donors can donate if last donation was at least 90 days ago
6. **OTP:** 6-digit code generated when donor accepts a request
7. **Pagination:** Default page size is 20. Adjust with `size` parameter

