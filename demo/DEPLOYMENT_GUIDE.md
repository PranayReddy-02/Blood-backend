# Blood Donor API - Deployment Guide

## Prerequisites

- Java 21 JDK
- MySQL 8.0+
- Maven 3.6+
- Git (for cloning)
- Docker (optional, for containerization)
- Docker Compose (optional, for containerized MySQL)

---

## Local Development Deployment

### Step 1: Clone and Setup

```bash
cd "Blood donor backend/demo"
mvn clean install
```

### Step 2: Configure Database

**Option A: Using existing MySQL**

```bash
mysql -u root -p < src/main/resources/schema.sql
```

**Option B: Using Docker MySQL**

Create `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: Palleyaksha@123
      MYSQL_DATABASE: blood_donor_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql

volumes:
  mysql_data:
```

Run:
```bash
docker-compose up -d
```

### Step 3: Update Configuration

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blood_donor_db
spring.datasource.username=root
spring.datasource.password=Palleyaksha@123
app.jwtSecret=YourVeryLongSecureSecretKeyFor256BitsOrMore...
app.jwtExpirationInMs=86400000
```

### Step 4: Run Application

```bash
mvn spring-boot:run
```

Server will start on `http://localhost:8080`

---

## Docker Containerization

### Step 1: Create Dockerfile

```dockerfile
FROM openjdk:21-slim
WORKDIR /app

# Copy Maven project
COPY pom.xml ./
COPY src ./src

# Build application
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests && \
    rm -rf src pom.xml

# Copy built JAR
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/blood_donor_db
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=Palleyaksha@123
ENV APP_JWTSECRET=YourVeryLongSecureSecretKeyFor256BitsOrMore...

# Run application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Step 2: Create docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: blood_donor_mysql
    environment:
      MYSQL_ROOT_PASSWORD: Palleyaksha@123
      MYSQL_DATABASE: blood_donor_db
      MYSQL_USER: donor_user
      MYSQL_PASSWORD: donor_pass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    networks:
      - blood_donor_network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  api:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: blood_donor_api
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/blood_donor_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: Palleyaksha@123
      APP_JWTSECRET: YourVeryLongSecureSecretKeyFor256BitsOrMore...
      APP_JWTEXPIRATIONINMS: 86400000
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - blood_donor_network
    restart: unless-stopped

volumes:
  mysql_data:

networks:
  blood_donor_network:
    driver: bridge
```

### Step 3: Build and Run

```bash
# Build images
docker-compose build

# Run containers
docker-compose up -d

# Check logs
docker-compose logs -f api

# Stop containers
docker-compose down
```

---

## Production Deployment

### Option 1: AWS EC2 Deployment

#### 1. Launch EC2 Instance

- AMI: Amazon Linux 2
- Instance Type: t3.medium or higher
- Storage: 30GB gp3
- Security Group: Allow ports 80, 443, 8080, 3306

#### 2. Install Dependencies

```bash
#!/bin/bash
yum update -y
yum install -y java-21-amazon-corretto-devel maven mysql80

# Start MySQL
systemctl start mysqld
systemctl enable mysqld
```

#### 3. Configure Application

```bash
# Create app directory
mkdir -p /opt/blood-donor-api
cd /opt/blood-donor-api

# Clone repository (if using Git)
git clone <repo-url> .

# Build application
mvn clean package -DskipTests

# Create systemd service file
sudo tee /etc/systemd/system/blood-donor-api.service > /dev/null <<EOF
[Unit]
Description=Blood Donor API Service
After=network.target mysql.service

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/blood-donor-api
EnvironmentFile=/opt/blood-donor-api/.env
ExecStart=/usr/bin/java -jar target/demo-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Create environment file
sudo tee /opt/blood-donor-api/.env > /dev/null <<EOF
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/blood_donor_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=SecurePassword123!
APP_JWTSECRET=YourVeryLongSecureSecretKeyFor256BitsOrMore...
APP_JWTEXPIRATIONINMS=86400000
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
EOF

# Start service
sudo systemctl daemon-reload
sudo systemctl start blood-donor-api
sudo systemctl enable blood-donor-api
```

#### 4. Configure Nginx Reverse Proxy

```bash
sudo yum install -y nginx

# Create nginx config
sudo tee /etc/nginx/conf.d/blood-donor-api.conf > /dev/null <<EOF
server {
    listen 80;
    server_name your-domain.com;

    # Redirect HTTP to HTTPS
    return 301 https://$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    client_max_body_size 20M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
EOF

# Start Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### 5. SSL Certificate (Let's Encrypt)

```bash
# Install Certbot
sudo yum install -y certbot python3-certbot-nginx

# Get certificate
sudo certbot certonly --nginx -d your-domain.com

# Auto-renewal
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer
```

---

### Option 2: Azure Container Instances

```bash
# Login to Azure
az login

# Create resource group
az group create --name blood-donor-rg --location eastus

# Create container registry
az acr create --resource-group blood-donor-rg \
    --name blooddonorregistry --sku Basic

# Login to ACR
az acr login --name blooddonorregistry

# Build and push image
docker build -t blooddonorapi:latest .
docker tag blooddonorapi:latest \
    blooddonorregistry.azurecr.io/blooddonorapi:latest
docker push blooddonorregistry.azurecr.io/blooddonorapi:latest

# Deploy to ACI
az container create \
    --resource-group blood-donor-rg \
    --name blood-donor-api \
    --image blooddonorregistry.azurecr.io/blooddonorapi:latest \
    --cpu 2 --memory 4 \
    --registry-login-server blooddonorregistry.azurecr.io \
    --registry-username <username> \
    --registry-password <password> \
    --environment-variables \
        SPRING_DATASOURCE_URL='jdbc:mysql://mysql-server:3306/blood_donor_db' \
        SPRING_DATASOURCE_USERNAME='root' \
        SPRING_DATASOURCE_PASSWORD='SecurePassword123!' \
        APP_JWTSECRET='YourVeryLongSecureSecretKeyFor256BitsOrMore...' \
    --ports 8080 \
    --protocol TCP
```

---

### Option 3: Kubernetes Deployment

Create `k8s-deployment.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: blood-donor-config
data:
  application.properties: |
    spring.datasource.url=jdbc:mysql://mysql-service:3306/blood_donor_db
    spring.datasource.username=root
    spring.datasource.password=${MYSQL_PASSWORD}
    app.jwtSecret=${JWT_SECRET}
    spring.jpa.hibernate.ddl-auto=update

---
apiVersion: v1
kind: Secret
metadata:
  name: blood-donor-secret
type: Opaque
stringData:
  mysql-password: "SecurePassword123!"
  jwt-secret: "YourVeryLongSecureSecretKeyFor256BitsOrMore..."

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: blood-donor-api
spec:
  replicas: 2
  selector:
    matchLabels:
      app: blood-donor-api
  template:
    metadata:
      labels:
        app: blood-donor-api
    spec:
      containers:
      - name: api
        image: blooddonorregistry.azurecr.io/blooddonorapi:latest
        ports:
        - containerPort: 8080
        env:
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: blood-donor-secret
              key: mysql-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: blood-donor-secret
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/admin/dashboard
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/donors/search?bloodGroup=O%2B
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: blood-donor-api-service
spec:
  selector:
    app: blood-donor-api
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

Deploy:
```bash
kubectl apply -f k8s-deployment.yaml
```

---

## Performance Optimization for Production

### 1. JVM Tuning

In startup script or Dockerfile:
```bash
java -Xms1G -Xmx2G \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:InitiatingHeapOccupancyPercent=35 \
  -Dspring.profiles.active=prod \
  -jar app.jar
```

### 2. Database Connection Pool

Update `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

### 3. Caching

Add Spring Cache:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("donors", "requests");
    }
}
```

### 4. Compression

```properties
server.compression.enabled=true
server.compression.min-response-size=1024
server.compression.excluded-mime-types=image/png,image/jpeg
```

---

## Monitoring & Logging

### 1. Application Logging

Enable in `application.properties`:
```properties
logging.level.root=WARN
logging.level.com.example.demo=INFO
logging.file.name=/var/log/blood-donor-api/application.log
logging.file.max-size=10MB
logging.file.max-history=10
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### 2. Health Check Endpoint

Add Spring Boot Actuator:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Configure:
```properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
```

### 3. Monitoring with Prometheus

```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
```

---

## Backup & Disaster Recovery

### MySQL Backup

```bash
#!/bin/bash
# Daily backup script
BACKUP_DIR="/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)

mysqldump -u root -p$MYSQL_PASSWORD blood_donor_db > \
  $BACKUP_DIR/backup_$DATE.sql

# Keep only last 7 days
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete
```

### Database Replication

Enable replication for high availability:
```sql
CHANGE MASTER TO
MASTER_HOST='primary-server',
MASTER_USER='replication',
MASTER_PASSWORD='password',
MASTER_LOG_FILE='mysql-bin.000001',
MASTER_LOG_POS=154;

START SLAVE;
```

---

## Troubleshooting

### Check Application Status

```bash
# View logs
tail -f /var/log/blood-donor-api/application.log

# Check service status
systemctl status blood-donor-api

# View Docker logs
docker logs -f blood-donor-api

# Check database connection
mysql -h localhost -u root -p -e "SELECT 1"
```

### Common Issues

**Port 8080 already in use:**
```bash
lsof -i :8080
kill -9 <PID>
```

**Database connection failed:**
- Verify MySQL is running
- Check credentials in configuration
- Ensure database tables exist

**JWT token invalid:**
- Verify JWT secret matches configuration
- Check token hasn't expired
- Ensure Authorization header format: `Bearer <token>`

---

## Version Updates

To update to a new version:

```bash
cd /opt/blood-donor-api

# Backup current version
cp target/demo-0.0.1-SNAPSHOT.jar \
  backup/demo-$(date +%Y%m%d-%H%M%S).jar

# Pull latest code
git pull origin main

# Rebuild
mvn clean package -DskipTests

# Restart
systemctl restart blood-donor-api

# Verify
systemctl status blood-donor-api
```

---

**Last Updated:** March 12, 2024
