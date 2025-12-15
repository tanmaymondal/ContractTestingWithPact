# Pact Contract Testing with Java

A complete demonstration of consumer-driven contract testing using Pact with Java, Spring Boot, and Pact Broker.

## 📋 Prerequisites

Before starting, ensure you have:
- ☕ **Java 17** or higher (`java -version`)
- 📦 **Maven 3.6+** (`mvn -version`)
- 🐳 **Docker Desktop** installed and running (`docker --version`)

## 🐳 Docker Setup for Pact Broker

This project uses Docker Compose to run a local Pact Broker instance with PostgreSQL database.

### Docker Compose Configuration

The `docker-compose.yml` file defines two services:

```yaml
services:
  postgres:
    image: postgres:14-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: pact
      POSTGRES_PASSWORD: pact
      POSTGRES_DB: pact_broker
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U pact"]
      interval: 10s
      timeout: 5s
      retries: 5

  pact-broker:
    image: pactfoundation/pact-broker:latest
    ports:
      - "9292:9292"
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      PACT_BROKER_DATABASE_URL: postgresql://pact:pact@postgres/pact_broker
      PACT_BROKER_BASIC_AUTH_USERNAME: pact
      PACT_BROKER_BASIC_AUTH_PASSWORD: pact
```

### Installing Docker Desktop

#### Windows
1. Download Docker Desktop from: https://www.docker.com/products/docker-desktop
2. Run the installer and follow the setup wizard
3. Restart your computer if prompted
4. Launch Docker Desktop from the Start menu
5. Wait for Docker to start (whale icon in system tray should be stable)
6. Verify installation:
   ```powershell
   docker --version
   docker compose version
   ```

#### macOS
1. Download Docker Desktop for Mac from: https://www.docker.com/products/docker-desktop
2. Open the `.dmg` file and drag Docker to Applications
3. Launch Docker from Applications
4. Grant necessary permissions when prompted
5. Verify installation:
   ```bash
   docker --version
   docker compose version
   ```

#### Linux
1. Install Docker Engine following the official guide: https://docs.docker.com/engine/install/
2. Install Docker Compose: https://docs.docker.com/compose/install/
3. Add your user to the docker group:
   ```bash
   sudo usermod -aG docker $USER
   newgrp docker
   ```
4. Verify installation:
   ```bash
   docker --version
   docker compose version
   ```

### Starting Pact Broker

**Start the services in detached mode:**
```powershell
docker compose up -d
```

**Expected output:**
```
[+] Running 2/2
 ✔ Container pactsetup-postgres-1      Started
 ✔ Container pactsetup-pact-broker-1   Started
```

**Check service status:**
```powershell
docker compose ps
```

**Expected output:**
```
NAME                        STATUS              PORTS
pactsetup-postgres-1        Up (healthy)        0.0.0.0:5432->5432/tcp
pactsetup-pact-broker-1     Up                  0.0.0.0:9292->9292/tcp
```

**Wait for services to be ready:** It takes approximately 30-45 seconds for the Pact Broker to be fully operational. You can check the logs:

```powershell
docker compose logs -f pact-broker
```

Press `Ctrl+C` to stop following logs.

### Accessing Pact Broker

Once the services are running, access the Pact Broker web interface:

- **URL:** http://localhost:9292
- **Username:** `pact`
- **Password:** `pact`

You should see the Pact Broker dashboard with no pacts initially.

### Managing Docker Services

**View logs:**
```powershell
# All services
docker compose logs

# Specific service
docker compose logs pact-broker
docker compose logs postgres

# Follow logs in real-time
docker compose logs -f
```

**Stop services (keeps data):**
```powershell
docker compose stop
```

**Start stopped services:**
```powershell
docker compose start
```

**Stop and remove containers (keeps data in volumes):**
```powershell
docker compose down
```

**Stop and remove everything including data:**
```powershell
docker compose down -v
```

**Restart services:**
```powershell
docker compose restart
```

**View running containers:**
```powershell
docker compose ps
```

### Troubleshooting Docker Setup

#### Issue: Docker command not found
**Solution:** Ensure Docker Desktop is installed and running. Restart your terminal after installation.

#### Issue: Port 9292 or 5432 already in use
**Solution:** 
```powershell
# Check what's using the port (Windows)
netstat -ano | findstr :9292
netstat -ano | findstr :5432

# Stop the conflicting service or change ports in docker-compose.yml
```

#### Issue: Permission denied (Linux)
**Solution:** Add your user to the docker group:
```bash
sudo usermod -aG docker $USER
newgrp docker
```

#### Issue: Services won't start
**Solutions:**
1. Check Docker Desktop is running
2. Check available disk space
3. View logs: `docker compose logs`
4. Try recreating containers: `docker compose down -v && docker compose up -d`

#### Issue: Pact Broker shows "Database connection failed"
**Solution:** 
1. Check if PostgreSQL is healthy: `docker compose ps`
2. Wait longer (PostgreSQL needs time to initialize)
3. Check logs: `docker compose logs postgres`
4. Restart services: `docker compose restart`

#### Issue: Cannot access http://localhost:9292
**Solutions:**
1. Verify container is running: `docker compose ps`
2. Check logs for errors: `docker compose logs pact-broker`
3. Wait 30-45 seconds after starting
4. Try accessing from host: http://127.0.0.1:9292
5. Check firewall settings

### Docker Cleanup

To completely remove all Pact Broker data and start fresh:

```powershell
# Stop and remove containers, networks, and volumes
docker compose down -v

# Remove unused images (optional)
docker image prune -a

# Start fresh
docker compose up -d
```

### Verifying Pact Broker is Working

After starting the services, verify everything is working:

```powershell
# Test API endpoint
curl http://localhost:9292

# Or use PowerShell
Invoke-WebRequest -Uri http://localhost:9292 -UseBasicParsing
```

You should see HTML content from the Pact Broker web interface.

## 🏗️ Project Structure

```
PactSetup/
├── consumer/                        # Consumer application
│   ├── src/
│   │   ├── main/java/              # HTTP client implementation
│   │   │   └── User.java           # User model
│   │   │   └── UserServiceClient.java  # HTTP client for User API
│   │   └── test/java/              # Pact consumer tests
│   │       └── UserServiceConsumerTest.java  # Defines contract expectations
│   └── pom.xml                     # Pact JUnit5 4.5.10, JUnit 5.10.1
│
├── provider/                        # Provider application (Spring Boot)
│   ├── src/
│   │   ├── main/java/              # REST API implementation
│   │   │   └── ProviderApplication.java  # Spring Boot app
│   │   │   └── User.java           # User model
│   │   │   └── UserController.java # REST endpoints
│   │   │   └── UserService.java    # Business logic
│   │   └── test/java/              # Pact provider verification
│   │       └── UserProviderPactTest.java  # Verifies against pacts
│   └── pom.xml                     # Spring Boot 2.7.14, Pact Provider
│
├── docker-compose.yml              # Pact Broker + PostgreSQL
├── run-all.ps1                     # Automated test script
├── cleanup.ps1                     # Cleanup script
└── README.md                       # This file
```

## 🚀 Quick Start

### Option A: Automated (Recommended)

Run everything with one command:
```powershell
.\run-all.ps1
```

This script will:
1. ✅ Start Pact Broker and PostgreSQL
2. ✅ Run consumer tests and generate pacts
3. ✅ Publish pacts to the broker
4. ✅ Run provider verification tests
5. ✅ Display results

### Option B: Manual Step-by-Step

#### Step 1: Start Pact Broker
```powershell
docker compose up -d
```

Wait 30-45 seconds for services to start, then access:
- **URL:** http://localhost:9292
- **Username:** `pact`
- **Password:** `pact`

#### Step 2: Run Consumer Tests
```powershell
cd consumer
mvn clean test
```

✅ **Expected Result:** Tests pass, pact file created in `target/pacts/UserConsumer-UserProvider.json`

#### Step 3: Publish Pacts to Broker
```powershell
mvn pact:publish
cd ..
```

✅ **Expected Result:** Pact appears in Pact Broker UI at http://localhost:9292

#### Step 4: Run Provider Verification
```powershell
cd provider
mvn clean test
cd ..
```

✅ **Expected Result:** Provider tests pass, verification results published to broker

#### Step 5: View Results

Open http://localhost:9292 in your browser:
- See the Consumer → Provider relationship diagram
- Green checkmark indicates successful verification
- Click on the relationship to view contract details

## 📖 How It Works

### Consumer Side (Contract Definition)

**File:** `consumer/src/test/java/com/example/pact/consumer/UserServiceConsumerTest.java`

```java
@Pact(consumer = "UserConsumer", provider = "UserProvider")
public V4Pact createPact(PactDslWithProvider builder) {
    return builder
        .given("user with id 1 exists")
        .uponReceiving("a request to get user by id")
            .path("/users/1")
            .method("GET")
        .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(expectedUserJson)
        .toPact(V4Pact.class);
}
```

**What happens:**
1. Test runs against a mock provider server
2. Contract expectations are defined (request/response)
3. Pact file is generated in JSON format
4. Pact is published to the broker

### Provider Side (Contract Verification)

**File:** `provider/src/test/java/com/example/pact/provider/UserProviderPactTest.java`

```java
@Provider("UserProvider")
@PactBroker(url = "http://localhost:9292", 
            authentication = @PactBrokerAuth(username = "pact", password = "pact"))
public class UserProviderPactTest {
    @State("user with id 1 exists")
    public void userExists() {
        // Setup test data for this state
    }
}
```

**What happens:**
1. Provider downloads pacts from broker
2. Spring Boot application starts
3. Pact framework replays consumer requests against real provider
4. Results are verified and published back to broker

## 🔧 Commands Reference

### Docker/Pact Broker
```powershell
# Start services
docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f pact-broker

# Stop services
docker compose down

# Stop and remove volumes
docker compose down -v
```

### Consumer
```powershell
cd consumer

# Run tests only
mvn clean test

# Publish pacts
mvn pact:publish

# Both in one command
mvn clean test pact:publish
```

### Provider
```powershell
cd provider

# Run verification tests
mvn clean test

# Run provider as standalone service
mvn spring-boot:run
# API available at: http://localhost:8080/users/1
```

### Cleanup
```powershell
# Use the cleanup script
.\cleanup.ps1

# Or manually
docker compose down
cd consumer && mvn clean && cd ..
cd provider && mvn clean && cd ..
```

## 🛠️ Troubleshooting

### Issue: Docker compose command not found
**Solution:** Use `docker compose` (with space) instead of `docker-compose` (with hyphen). Docker Compose V2 is integrated into Docker Desktop.

### Issue: Pact Broker not accessible
```powershell
# Check if containers are running
docker compose ps

# Check logs
docker compose logs pact-broker

# Restart if needed
docker compose restart
```

### Issue: Consumer tests fail with "ClassNotFoundException"
**Cause:** Version incompatibility between Pact and JUnit
**Solution:** This project uses Pact 4.5.10 and JUnit 5.10.1 (already fixed)

### Issue: Provider verification fails
**Checklist:**
1. ✅ Consumer pacts published to broker first
2. ✅ Provider is running on port 8080
3. ✅ Pact Broker is accessible
4. ✅ State setup methods exist in provider test

### Issue: Pact not appearing in broker
```powershell
# Verify broker is running
curl http://localhost:9292

# Check publish command output
mvn pact:publish

# Verify credentials in pom.xml
# Username: pact, Password: pact
```

## 📦 Key Dependencies

### Consumer
- **Pact JUnit5:** 4.5.10
- **JUnit Jupiter:** 5.10.1
- **Maven Surefire:** 3.2.2
- **Apache HttpClient:** 4.5.14
- **Gson:** 2.10.1

### Provider
- **Spring Boot:** 2.7.14
- **Pact Provider JUnit5:** 4.3.19
- **JUnit Jupiter:** 5.9.3

## ✨ What You'll See

### After Consumer Test
```
consumer/target/pacts/UserConsumer-UserProvider.json
```
Contains the contract in JSON format.

### In Pact Broker UI
- Visual diagram showing relationships
- Contract details (requests, responses, states)
- Verification status with timestamps
- Matrix view showing compatibility

### After Provider Verification
- Green checkmark ✅ in broker UI
- Verification results with details
- Can-i-deploy status information

## 🎯 Key Concepts

| Concept | Description |
|---------|-------------|
| **Consumer** | Service that makes HTTP requests (client) |
| **Provider** | Service that handles HTTP requests (API) |
| **Pact** | Contract file describing expected interactions |
| **Pact Broker** | Repository for storing and sharing pacts |
| **State** | Provider condition needed for test (e.g., "user exists") |
| **Verification** | Testing that provider implements the contract |
| **Can-i-deploy** | Check if consumer & provider versions are compatible |

## 📚 Additional Resources

- [Pact Documentation](https://docs.pact.io/)
- [Pact JVM GitHub](https://github.com/pact-foundation/pact-jvm)
- [Pact Broker Documentation](https://docs.pact.io/pact_broker)
- [Spring Boot with Pact](https://docs.pact.io/implementation_guides/jvm/provider/spring)

## 📝 License

This is a demonstration project for learning Pact contract testing.

---

**Need Help?** Check the troubleshooting section or open an issue on GitHub.

#   C o n t r a c t T e s t i n g W i t h P a c t  
 