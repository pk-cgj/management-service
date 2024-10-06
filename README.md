# Microservices Project with Keycloak and PostgreSQL

This project implements a microservices architecture using Spring Boot, with Keycloak for authentication and PostgreSQL for data storage.

## Project Structure

```
management-service/
├── migration/
│   ├── hazelcast/
│   │   ├── hazelcast.yaml
│   │   └── hazelcast-client.yaml
│   ├── keycloak/
│   │   └── management-realm.json
│   └── postgres/
│       └── init-db.sql
├── order-management/
├── user-management/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   │           ├── application.yaml
│   │           ├── local-hazelcast.yaml
│   │           └── docker-application.yaml
│   ├── build.gradle
│   └── Dockerfile
├── docker-compose.yml
├── Dockerfile.keycloak
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
- PostgreSQL: http://localhost:5432
    - Databases: keycloak, user_service, order_service
- Kafka: http://localhost:29092
- Zookeeper: http://localhost:22181
- Hazelcast: http://localhost:5701
- Hazelcast Management Center: http://localhost:8088

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
- GET /api/users/{id}: Accessible by USER (only their own), MANAGER, and ADMIN
- PUT /api/users/{id}: Accessible by USER (only their own), MANAGER, and ADMIN
- GET /api/users: Accessible by MANAGER and ADMIN
- POST /api/users: Accessible by MANAGER and ADMIN
- DELETE /api/users/{id}: Accessible by ADMIN only

### Order Management Service

Endpoints and access:
- GET /api/orders: USER can only view their own, MANAGER and ADMIN can view all
- POST /api/orders: Accessible by all roles (USER, MANAGER, ADMIN)
- GET /api/orders/{id}: USER can only view their own, MANAGER and ADMIN can view all
- PUT /api/orders/{id}: Accessible by MANAGER (status updates only) and ADMIN
- DELETE /api/orders/{id}: Accessible by ADMIN only

## Imported Users and Roles

The following sample users and roles are imported into Keycloak:

1. Regular User:
    - Username: user1
    - Password: password
    - Role: USER

2. Manager:
    - Username: manager1
    - Password: password
    - Role: MANAGER

3. Admin:
    - Username: admin1
    - Password: password
    - Role: ADMIN

For detailed role definitions, please refer to the original README.

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

For more detailed information about each service, refer to their respective documentation or configuration files.