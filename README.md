# Microservices Project with Keycloak and PostgreSQL

This project implements a microservices architecture using Spring Boot, with Keycloak for authentication and PostgreSQL for data storage.

## Project Structure

```
management-service/
├── keycloak/
│   ├── docker-compose.yml
│   └── management-realm.json
├── postgre/
│   ├── docker-compose.yml
│   ├── init-db.sql
│   └── README.md
└── [other microservice directories]
```

## Prerequisites

- Docker and Docker Compose
- Java 21
- Gradle

## Setup Instructions

1. Start PostgreSQL (this will also create the shared network and initialize the databases):
   ```
   cd postgre
   docker-compose up -d
   ```

2. Start Keycloak:
   ```
   cd ../keycloak
   docker-compose up -d
   ```

3. Access Keycloak admin console:
    - URL: http://localhost:8080/admin/
    - Username: admin
    - Password: admin

## PostgreSQL Setup

The PostgreSQL container is configured to automatically create the necessary databases and users on startup. This is done through an initialization script (`init-db.sql`) that is executed when the container is first created.

The script does the following:
1. Creates databases for Keycloak, User Service, and Order Service.
2. Creates users for each database with appropriate permissions.
3. Sets up initial schemas for User Service and Order Service databases.

You can find this script in the `postgre` directory. If you need to modify the database structure or add initial data, you can edit this script.

Note: The initialization script only runs if the data directory is empty (i.e., when the container is first created). If you need to rerun the script, you'll need to remove the existing data volume first.

To remove the existing data volume and rerun the initialization:
```bash
docker-compose down -v
docker-compose up -d
```

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

### Role Definitions:
- USER:
    - Can view and modify their own user profile
    - Can view their own orders
    - Can create new orders
    - Cannot modify or delete any orders
- MANAGER:
    - Can view and manage all user profiles (except admins)
    - Can view all orders
    - Can create new orders
    - Can modify order status (e.g., mark as shipped, cancelled)
    - Cannot delete orders
- ADMIN:
    - Has full access to all functionalities
    - Can manage all users, including other admins
    - Can perform any operation on orders, including deletion

## Services

- Keycloak: http://localhost:8080
- PostgreSQL: localhost:5432
    - Databases: keycloak, user_service, order_service

## Microservices Configuration

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

## Stopping Services

To stop the services:
```
docker-compose down
```
(Run this command in both postgre and keycloak directories)

To remove the shared network:
```
docker network rm microservices-network
```

Note: Only remove the network if you're sure no other services are using it.


## Troubleshooting

If you encounter issues:

1. Ensure all containers are running:
   ```
   docker ps
   ```

2. Check container logs:
   ```
   docker logs shared-postgres
   docker logs keycloak-server
   ```

3. Verify network connectivity:
   ```
   docker network inspect microservices-network
   ```

4. If Keycloak fails to start due to permission issues:
    - Ensure PostgreSQL is fully initialized before starting Keycloak.
    - Check that the `init-db.sql` script is granting the correct permissions to the Keycloak user.
    - You can manually grant permissions by connecting to the PostgreSQL container:
      ```
      docker exec -it shared-postgres psql -U postgres
      ```
      Then run:
      ```sql
      \c keycloak
      GRANT ALL ON SCHEMA public TO keycloak;
      GRANT ALL ON ALL TABLES IN SCHEMA public TO keycloak;
      GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO keycloak;
      ```

5. If database issues persist, you may need to reinitialize the databases:
   ```
   docker-compose down -v
   docker-compose up -d
   ```

Remember to always check the logs for the most up-to-date error messages and status information.
