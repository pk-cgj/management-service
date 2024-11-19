# Microservices Project with Keycloak and PostgreSQL

This project implements a microservices architecture using Spring Boot, with Keycloak for authentication and PostgreSQL for data storage.

## Project Structure

```
management-service/
├── migration/
│   ├── hazelcast/
│   │   └── hazelcast.yaml
│   ├── keycloak/
│   │   └── management-realm.json
│   └── postgres/
│       └── init-db.sql
├── order-management/
│   ├── .gradle/
│   ├── build/
│   ├── gradle/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           ├── application.yaml
│   │           └── hazelcast-client.yaml
│   ├── build.gradle
│   ├── Dockerfile
│   ├── gradlew
│   └── gradlew.bat
├── user-management/
│   ├── .gradle/
│   ├── .idea/
│   ├── build/
│   ├── gradle/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           ├── application.yaml
│   │           └── hazelcast-client.yaml
│   ├── build.gradle
│   └── Dockerfile
├── docker-compose.yml
└── README.md
```

## Prerequisites

- Docker and Docker Compose
- Java 21
- Gradle

## Setup Instructions

1. Start all services using Docker Compose:
   ```
   docker-compose up -d
   ```

2. Access Keycloak admin console:
   - URL: http://localhost:8888/admin/
   - Username: admin
   - Password: admin

## Services

- Keycloak: http://localhost:8888
- User Management Service: http://localhost:8080
- Order Management Service: http://localhost:8081
- PostgreSQL: http://localhost:5432
   - Databases: keycloak, user_service, order_service
- Kafka: http://localhost:29092
- Zookeeper: http://localhost:22181
- Hazelcast: http://localhost:5701

## Configuration

The `docker-application.yaml` file in the `user-management/src/main/resources/` directory contains the configuration for the User Management Service. Key configurations include:

- Server port: 8080
- Swagger UI path: /swagger-ui.html
- OAuth2 resource server and client configurations
- Kafka consumer and producer settings
- PostgreSQL datasource configuration
- Hazelcast client configuration

For detailed configuration, please refer to the `docker-application.yaml` file.

## Microservices

### User Management Service

Endpoints and access:

- POST /api/login: Public endpoint for user authentication
- GET /api/user-info: Authenticated users can retrieve their own information
- GET /api/users/me: Accessible by USER and ADMIN (retrieve current user)
- GET /api/users/me/details: Accessible by USER and ADMIN (retrieve current user details)
- PUT /api/users/me: Accessible by USER and ADMIN (update own profile)
- GET /api/admin/users: Accessible by ADMIN only (retrieve all users)
- GET /api/admin/users/{id}: Accessible by users with 'order:read' authority or ADMIN
- GET /api/admin/users/{id}/details: Accessible by ADMIN only
- POST /api/admin/users: Accessible by ADMIN only (create new user)
- PUT /api/admin/users/{id}: Accessible by ADMIN only (update user)
- DELETE /api/admin/users/{id}: Accessible by ADMIN only (delete user)

### Order Management Service

Endpoints and access:

- GET /api/orders: Accessible by ADMIN only (retrieve all orders)
- GET /api/orders/user/{userId}: Accessible by users with 'order:read' authority or ADMIN
- GET /api/orders/{id}: Accessible by USER (retrieve specific order)
- POST /api/orders: Accessible by USER and ADMIN (create new order)
- PUT /api/orders/{id}: Accessible by ADMIN only (update order)
- DELETE /api/orders/{id}: Accessible by ADMIN only (delete order)

## Authentication and Authorization

The project uses Keycloak for authentication and authorization. Users can obtain a token by sending a POST request to `/api/login` with their username and password. This token should be included in the Authorization header for subsequent requests.

## Imported Users and Roles

The following sample users and roles are imported into Keycloak:

1. Regular User:
   - Username: user1
   - Password: password
   - Role: USER

2. Admin:
   - Username: admin1
   - Password: password
   - Role: ADMIN

## Data Persistence

The project uses PostgreSQL for data storage. Ensure that the database is properly initialized and that the application has the correct database connection details in the configuration.

## Caching

Hazelcast is integrated for caching. The Hazelcast configuration can be found in the `hazelcast.yaml` and `hazelcast-client.yaml` files.

## Messaging

Kafka is used for messaging between services. Ensure that Kafka is properly configured and that the services have the correct Kafka connection details.

## Stopping Services

To stop all services:

```
docker-compose down
```

To remove all associated volumes:

```
docker-compose down -v
```

## Troubleshooting

If you encounter any issues, please check the logs of individual services using:

```
docker-compose logs [service_name]
```

Replace `[service_name]` with the name of the service you want to inspect (e.g., user-management, keycloak, postgres, etc.).

## Additional Notes

- The project uses Spring Security for endpoint protection. Ensure that the security configurations are properly set up in both services.
- Implement proper error handling and validation for all endpoints.
- Use environment variables for sensitive information like database credentials and Keycloak client secrets.
- Implement logging throughout the application for better debugging and monitoring.
- Consider implementing rate limiting and other security measures to protect your APIs.
- Regularly update dependencies and apply security patches.

For more detailed information about each service, refer to their respective documentation or configuration files.