# Yushan API Gateway

API Gateway and routing service for Yushan Novel Platform microservices architecture.

## Features

- Single entry point for all microservices
- Service discovery via Eureka
- Request/Response logging
- CORS configuration
- Health monitoring
- Load balancing across service instances
- Service routing and path rewriting

## Architecture

The API Gateway routes requests to the following microservices:

- **User Service** (port 8081) - Authentication & user management
- **Content Service** (port 8082) - Novels & chapters
- **Analytics Service** (port 8083) - Rankings & history
- **Engagement Service** (port 8084) - Comments, reviews & votes
- **Gamification Service** (port 8085) - EXP, Yuan & achievements

## Running Locally

### Prerequisites
- Java 21+
- Maven 3.8+
- Eureka Server running on port 8761
- All microservices running on their respective ports

### Start the Gateway
```bash
./mvnw spring-boot:run
```

The gateway will start on port 8080.

### Build JAR
```bash
./mvnw clean package -DskipTests
java -jar target/api-gateway-1.0.0.jar
```

## API Routes

All requests go through `http://localhost:8080/api/*`

### User Service Routes
| Method | Endpoint | Authentication | Description |
|--------|----------|---------------|-------------|
| POST | `/api/auth/login` | Public | User login |
| POST | `/api/auth/register` | Public | User registration |
| GET | `/api/users/{id}` | Required | Get user profile |
| PUT | `/api/users/{id}` | Required | Update user profile |
| GET | `/api/library` | Required | Get user library |

### Content Service Routes
| Method | Endpoint | Authentication | Description |
|--------|----------|---------------|-------------|
| GET | `/api/novels` | Public | List all novels |
| POST | `/api/novels` | Required | Create novel |
| GET | `/api/novels/{id}` | Public | Get novel details |
| GET | `/api/chapters/{id}` | Required | Get chapter content |
| POST | `/api/chapters` | Required | Create chapter |

### Engagement Service Routes
| Method | Endpoint | Authentication | Description |
|--------|----------|---------------|-------------|
| POST | `/api/comments` | Required | Add comment |
| GET | `/api/comments/{chapterId}` | Public | Get comments |
| POST | `/api/reviews` | Required | Add review |
| GET | `/api/reviews/{novelId}` | Public | Get reviews |
| POST | `/api/votes` | Required | Vote on content |

### Gamification Service Routes
| Method | Endpoint | Authentication | Description |
|--------|----------|---------------|-------------|
| GET | `/api/exp/{userId}` | Required | Get user EXP |
| POST | `/api/exp` | Required | Award EXP |
| GET | `/api/yuan/{userId}` | Required | Get Yuan balance |
| POST | `/api/yuan` | Required | Update Yuan |
| GET | `/api/achievements/{userId}` | Required | Get achievements |

### Analytics Service Routes
| Method | Endpoint | Authentication | Description |
|--------|----------|---------------|-------------|
| GET | `/api/rankings/novels` | Public | Get novel rankings |
| GET | `/api/rankings/users` | Public | Get user rankings |
| GET | `/api/history/{userId}` | Required | Get reading history |
| GET | `/api/analytics/{novelId}` | Required | Get novel analytics |

## Authentication

**Note**: JWT authentication is handled by individual microservices, not by the API Gateway. Each service validates JWT tokens independently.

### Getting a Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Using the Token

```bash
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Public Endpoints

These endpoints do NOT require authentication (handled by individual services):
- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/api/v1/auth/refresh`
- `/api/v1/novels` (browsing)
- `/api/v1/categories` (browsing)
- `/actuator/health`

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Gateway port | `8080` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka server URL | `http://localhost:8761/eureka/` |

### application.yml

Key configurations:

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Docker

### Build Image

```bash
docker build -t yushan-api-gateway:latest .
```

### Run Container

```bash
# Connect to local services
docker run -d \
  --name yushan-api-gateway \
  -p 8080:8080 \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ \
  yushan-api-gateway:latest
```

### Check Logs

```bash
docker logs -f yushan-api-gateway
```

### Stop and Remove

```bash
docker stop yushan-api-gateway
docker rm yushan-api-gateway
```

## Docker Compose

If running the full stack:

```yaml
services:
  api-gateway:
    build: ./yushan-api-gateway
    ports:
      - "8080:8080"
    environment:
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server
```

Run with:
```bash
docker-compose up -d api-gateway
```

## Testing

### Run Tests

```bash
./mvnw test
```

### Test Routing

```bash
# Test public endpoint
curl http://localhost:8080/api/auth/login

# Test health check
curl http://localhost:8080/actuator/health

# Test routing to User Service
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Test routing to Content Service
curl http://localhost:8080/api/novels

# Test routing to Analytics Service
curl http://localhost:8080/api/rankings/novels
```

## Monitoring

### Gateway Routes

View all configured routes:
```bash
curl http://localhost:8080/actuator/gateway/routes
```

### Eureka Dashboard

Check service registration:
```
http://localhost:8761
```

You should see `API-GATEWAY` listed among registered services.

## Troubleshooting

### Issue: Gateway can't connect to services

**Check:**
1. Eureka Server is running on port 8761
2. All microservices are registered in Eureka
3. Service names match the routes in `application.yml`

```bash
# Check Eureka for registered services
curl http://localhost:8761/eureka/apps
```

### Issue: JWT validation fails

**Check:**
1. JWT secret matches the User Service configuration
2. Token format is correct: `Bearer <token>`
3. Token hasn't expired

### Issue: CORS errors

**Solution:**
CORS is configured globally to allow all origins. If you need to restrict origins, update `CorsConfig.java`:

```java
corsConfig.setAllowedOrigins(List.of("http://localhost:3000", "https://yourdomain.com"));
```

### Issue: Port 8080 already in use

```bash
# Find process using port 8080
lsof -i :8080  # Mac/Linux
netstat -ano | findstr :8080  # Windows

# Change port in application.yml or via environment variable
SERVER_PORT=8090 ./mvnw spring-boot:run
```

## Development

### Project Structure

```
yushan-api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/yushan/gateway/
│   │   │   ├── ApiGatewayApplication.java
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── GatewayConfig.java
│   │   │   └── filter/
│   │   │       ├── AuthenticationFilter.java
│   │   │       └── LoggingFilter.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-docker.yml
│   └── test/
├── Dockerfile
├── pom.xml
└── README.md
```

### Adding New Routes

To add routes for a new service, update `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: new-service
          uri: lb://new-service
          predicates:
            - Path=/api/newservice/**
          filters:
            - RewritePath=/api/newservice/(?<segment>.*), /${segment}
```

### Adding Authentication Exceptions

To make endpoints public, update `AuthenticationFilter.java`:

```java
private static final List<String> PUBLIC_PATHS = List.of(
    "/api/auth/login",
    "/api/auth/register",
    "/api/your-new-public-endpoint"
);
```

## Performance

### Load Balancing

The gateway automatically load balances requests across multiple instances of the same service using Eureka's service registry.

### Timeouts

Default timeout is 30 seconds. To adjust:

```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 5000
        response-timeout: 30s
```

## Security

### Best Practices

1. **Always use HTTPS in production**
2. **Keep JWT secret secure** - use environment variables
3. **Rotate JWT secrets regularly**
4. **Implement rate limiting** (consider Spring Cloud Gateway rate limiter)
5. **Monitor failed authentication attempts**

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests: `./mvnw test`
4. Submit a pull request

## License

This project is part of the Yushan Novel Platform.

## Support

For issues or questions:
- Check the troubleshooting section
- Review service logs: `docker logs yushan-api-gateway`
- Verify Eureka registration: `http://localhost:8761`

## Version

Current version: 1.0.0

## Dependencies

- Spring Boot 3.4.10
- Spring Cloud Gateway 2024.0.2
- Spring Cloud Netflix Eureka Client
- JJWT 0.12.3
- Java 21
