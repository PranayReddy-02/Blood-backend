# Build & Run Guide - Blood Donor Backend API

Complete step-by-step instructions for building, configuring, and running the Blood Donor Backend API.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Database Setup](#database-setup)
3. [Maven Build](#maven-build)
4. [Running the Application](#running-the-application)
5. [Verifying Installation](#verifying-installation)
6. [Troubleshooting](#troubleshooting)
7. [Next Steps](#next-steps)

---

## Prerequisites

### Required Software

Verify you have these installed before proceeding:

```bash
# Check Java 21
java -version
# Should output: openjdk version "21" or similar

# Check Maven
mvn -version
# Should output: Apache Maven 3.6.3+ or similar

# Check MySQL
mysql --version
# Should output: mysql Ver XXX or similar
```

### Required Components

- ✅ **Java 21 LTS** (from Oracle or OpenJDK)
- ✅ **Maven 3.6.3+** (build tool)
- ✅ **MySQL 8.0+** (database)
- ✅ **Internet connection** (for downloading dependencies)

### Optional but Recommended

- **Git** - Version control
- **Postman** - API testing
- **MySQL Workbench** - Database management GUI
- **IntelliJ IDEA** or **VS Code** - IDE

---

## Database Setup

### Step 1: Start MySQL Service

**Windows:**
```bash
net start MySQL80
```

**macOS:**
```bash
brew services start mysql
```

**Linux (Ubuntu/Debian):**
```bash
sudo systemctl start mysql
```

### Step 2: Verify MySQL Connection

```bash
mysql -u root -p
```

When prompted, enter password: `Palleyaksha@123`

You should see the MySQL prompt:
```
mysql>
```

Type `exit` to quit.

### Step 3: Create Database

Copy the entire content from `schema.sql` and execute it in MySQL:

```bash
# Option 1: Using file redirection
mysql -u root -p < src/main/resources/schema.sql
# Enter password: Palleyaksha@123

# Option 2: Using MySQL shell
mysql -u root -p
# Enter password: Palleyaksha@123
# Then paste the contents of schema.sql
# Or use: source src/main/resources/schema.sql;
```

### Step 4: Verify Database Creation

```bash
mysql -u root -p
# Enter password: Palleyaksha@123

# List databases
SHOW DATABASES;

# Should see: blood_donor_db in the list

# Use the database
USE blood_donor_db;

# List tables
SHOW TABLES;

# Should see:
# - users
# - donor_details
# - blood_requests
# - request_responses
# - donation_history

exit;
```

---

## Maven Build

### Step 1: Navigate to Project Directory

```bash
cd path/to/Blood\ donor\ backend/demo/
```

### Step 2: Clean Previous Builds (Optional)

```bash
mvn clean
```

This removes old compiled files.

### Step 3: Download Dependencies

```bash
mvn dependency:resolve
```

This downloads all required JAR files from Maven Central Repository.

**Expected Output:**
```
[INFO] BUILD SUCCESS
```

### Step 4: Compile Source Code

```bash
mvn compile
```

This compiles all Java source files to `.class` files.

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXXs
```

### Step 5: Run Tests (Optional)

```bash
mvn test
```

This runs any unit tests defined in the project.

### Step 6: Package Application

```bash
mvn package -DskipTests
```

This creates a JAR file: `target/demo-0.0.1-SNAPSHOT.jar`

**Full Build (One Command):**
```bash
mvn clean install
```

This runs all steps (clean, compile, test, package) in sequence.

---

## Running the Application

### Option 1: Using Maven (Recommended for Development)

```bash
mvn spring-boot:run
```

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.0.3)

2024-03-12 10:30:45.123  INFO 12345 --- [           main] c.e.d.DemoApplication                    : Starting DemoApplication
...
2024-03-12 10:30:50.456  INFO 12345 --- [           main] c.e.d.DemoApplication                    : Started DemoApplication in 4.321 seconds

Tomcat started on port(s): 8080 (http)
```

**Application is now running at:** `http://localhost:8080`

### Option 2: Using JAR File (Production)

```bash
# First, build the JAR
mvn clean package -DskipTests

# Then run it
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Option 3: Using IDE (Recommended for Development)

**IntelliJ IDEA:**
1. Open project in IntelliJ
2. Right-click on `DemoApplication.java`
3. Select **Run 'DemoApplication.main()'**
4. Or press `Shift + F10`

**VS Code with Spring Boot Extension:**
1. Open Command Palette (`Ctrl + Shift + P`)
2. Search for "Spring Boot: Start"
3. Select the project

---

## Verifying Installation

### Quick Verification (30 seconds)

```bash
# Test if application is running
curl http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test@1234"}'
```

Expected response (401 Unauthorized is normal):
```json
{
  "error": "User not found or invalid credentials",
  "status": 401,
  "timestamp": "2024-03-12T10:35:20.123Z"
}
```

### Full Verification (5 minutes)

Follow the **Complete User Flow** in [QUICKSTART.md](QUICKSTART.md):

1. **Register a new user (Donor)**
```bash
curl http://localhost:8080/api/auth/register -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "email":"donor@example.com",
    "password":"Donor@1234",
    "name":"John Donor",
    "phoneNumber":"9876543210",
    "city":"Mumbai",
    "role":"DONOR",
    "bloodGroup":"O+"
  }'
```

Expected response (201 Created):
```json
{
  "id": 1,
  "email": "donor@example.com",
  "name": "John Donor",
  "role": "DONOR"
}
```

2. **Login with that user**
```bash
curl http://localhost:8080/api/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "email":"donor@example.com",
    "password":"Donor@1234"
  }'
```

Expected response (200 OK):
```json
{
  "email": "donor@example.com",
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400000
}
```

3. **Search donors** (public endpoint, no auth required)
```bash
curl "http://localhost:8080/api/donors/search?bloodGroup=O%2B&city=Mumbai&page=0&size=10" \
  -H "Accept: application/json"
```

Expected response (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "name": "John Donor",
      "bloodGroup": "O+",
      "city": "Mumbai",
      "isAvailable": true,
      "isEligible": true,
      "totalDonations": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

## Application Logs and Monitoring

### View Real-time Logs

When running with `mvn spring-boot:run`, logs are displayed in the terminal:

```
2024-03-12 10:35:45.123  INFO 12345 --- [nio-8080-exec-1] c.e.d.controller.AuthController : POST /api/auth/register
2024-03-12 10:35:46.456  INFO 12345 --- [nio-8080-exec-1] c.e.d.service.AuthService : User registered successfully
```

### Increase Logging Level (Debug Mode)

Edit `application.properties`:
```properties
# Change from
logging.level.com.example.demo=DEBUG

# To
logging.level.com.example.demo=TRACE
```

Then restart the application.

### Check Application Health

Spring Boot Actuator shows application health:
```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

---

## Troubleshooting

### Issue 1: "Port 8080 already in use"

**Solution 1:** Kill the process using port 8080
```bash
# Find process ID
netstat -ano | findstr :8080

# Kill it
taskkill /PID <process_id> /F
```

**Solution 2:** Use a different port
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

### Issue 2: "Cannot connect to database"

**Error Message:**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**Solutions:**
1. Verify MySQL is running: `mysql -u root -p`
2. Check database credentials in `application.properties`
3. Verify database exists: `SHOW DATABASES;`
4. Check port 3306 is not blocked by firewall

### Issue 3: "No suitable driver found"

**Solution:**
```bash
# Ensure MySQL connector is in dependencies
mvn dependency:tree | grep mysql

# If missing, run:
mvn clean install
```

### Issue 4: "ClassNotFoundException"

**Solution:**
```bash
# Complete rebuild
mvn clean
mvn compile
mvn package
mvn spring-boot:run
```

### Issue 5: Maven Build Fails

**Check Maven configuration:**
```bash
mvn -version

# Update Maven if needed
mvn -U clean install

# Try offline compilation
mvn -o compile
```

### Issue 6: Out of Memory Error

**Solution:** Increase JVM memory
```bash
export MAVEN_OPTS="-Xmx1024m -Xms512m"
mvn spring-boot:run
```

### Debug Mode

To run with STS debugger:
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

---

## Directory Structure After Build

```
demo/
├── src/                          # Source code
├── target/                       # Compiled output
│   ├── classes/                  # Compiled Java classes
│   ├── demo-0.0.1-SNAPSHOT.jar  # Executable JAR
│   └── maven-archiver/
├── pom.xml                       # Maven configuration
└── [Documentation files]
```

---

## Environment Configuration

### application.properties Settings

**Database:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blood_donor_db
spring.datasource.username=root
spring.datasource.password=Palleyaksha@123
```

**JWT:**
```properties
app.jwtSecret=YourVeryLongSecureSecretKeyFor256BitsOrMore...
app.jwtExpirationInMs=86400000
```

**Server:**
```properties
server.port=8080
spring.application.name=blood-donor-api
```

**Logging:**
```properties
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
```

---

## Performance Monitoring

### Monitor In Real-time

```bash
# While application is running, in another terminal:

# Check memory usage
jps -l -v

# Monitor with JVM tools
jstat -gc -h10 <process_id> 1000

# Check active threads
jstack <process_id>
```

### Test Database Connection

```bash
# See if queries execute quickly
mysql -u root -p -D blood_donor_db -e "SELECT COUNT(*) FROM users;"

# Check table sizes
mysql -u root -p -D blood_donor_db -e "SHOW TABLE STATUS;"
```

---

## Next Steps

### After Successful Build and Run

1. ✅ **Test API Endpoints**
   - Follow [QUICKSTART.md](QUICKSTART.md)
   - Test 20+ endpoints in [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

2. ✅ **Understand Architecture**
   - Read [ARCHITECTURE.md](ARCHITECTURE.md)
   - Review entity relationships
   - Study service layer design

3. ✅ **Explore Codebase**
   - Read [FILE_INDEX.md](FILE_INDEX.md) for file organization
   - Study service implementations
   - Review controller endpoints

4. ✅ **Set Up IDE**
   - Import project into IntelliJ/VS Code
   - Configure Spring Boot run configuration
   - Set breakpoints for debugging

5. ✅ **Run Tests**
   - Follow [TESTING_GUIDE.md](TESTING_GUIDE.md)
   - Create unit tests for services
   - Set up integration tests

6. ✅ **Deploy**
   - When ready, follow [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)
   - Deploy to Docker/AWS/Azure/Kubernetes

---

## Quick Command Reference

| Task | Command |
|------|---------|
| Start MySQL | `net start MySQL80` (Windows) |
| Create Database | `mysql -u root -p < src/main/resources/schema.sql` |
| Build Project | `mvn clean install` |
| Run App | `mvn spring-boot:run` |
| Run Tests | `mvn test` |
| Build JAR | `mvn clean package` |
| Run JAR | `java -jar target/demo-0.0.1-SNAPSHOT.jar` |
| Check Health | `curl http://localhost:8080/actuator/health` |
| Test Login | `curl -X POST http://localhost:8080/api/auth/login ...` |

---

## Common Issues Solved

| Error | Cause | Solution |
|-------|-------|----------|
| Port 8080 in use | Another process using port | Change port or kill process |
| Cannot connect to DB | MySQL not running or wrong credentials | Start MySQL, verify credentials |
| Build fails | Missing dependencies | Run `mvn clean install` |
| ClassNotFoundException | Incomplete build | Run full Maven rebuild |
| Out of memory | JVM heap too small | Increase MAVEN_OPTS |
| Slow startup | Large dependency download | Run `mvn dependency:resolve` |

---

## Getting Help

If you encounter issues:

1. **Check logs** - Most issues are in application logs
2. **Verify prerequisites** - Ensure Java, Maven, MySQL are installed
3. **Read documentation** - Check [README.md](README.md) and [TROUBLESHOOTING.md](DEPLOYMENT_GUIDE.md#troubleshooting)
4. **Review code** - Examine service implementations in `src/main/java`
5. **Test manually** - Use cURL commands from [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## Success Indicators

You'll know everything is working when:

✅ Maven build completes with "BUILD SUCCESS"  
✅ Application starts and shows "Started in X.XXX seconds"  
✅ Database tables are created successfully  
✅ API endpoints return proper responses  
✅ JWT tokens are generated on successful login  
✅ Pagination works on list endpoints  
✅ Role-based access control enforces permissions  

---

**Congratulations! Your Blood Donor Backend API is ready to use! 🎉**

For detailed API usage and user flows, see [QUICKSTART.md](QUICKSTART.md).

For deployment to production, see [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md).

