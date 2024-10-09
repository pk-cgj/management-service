-- Create Keycloak database and user
CREATE DATABASE keycloak;
CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak_password';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Connect to keycloak database
\c keycloak

-- Create schema and grant permissions for Keycloak
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL ON ALL TABLES IN SCHEMA public TO keycloak;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak;

-- Set the search path for Keycloak
ALTER DATABASE keycloak SET search_path TO public;

-- Create User Service database and user
CREATE DATABASE user_service;
CREATE USER user_service WITH ENCRYPTED PASSWORD 'user_service_password';
GRANT ALL PRIVILEGES ON DATABASE user_service TO user_service;

-- Connect to user_service database
\c user_service

-- Create users table
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)              NOT NULL UNIQUE,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    password   VARCHAR(60)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Grant privileges to user_service user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO user_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO user_service;

-- Create Order Service database and user
CREATE DATABASE order_service;
CREATE USER order_service WITH ENCRYPTED PASSWORD 'order_service_password';
GRANT ALL PRIVILEGES ON DATABASE order_service TO order_service;

-- Connect to order_service database
\c order_service

-- Create orders table
CREATE TABLE orders
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL,
    product    VARCHAR(100)             NOT NULL,
    quantity   DECIMAL(10, 2)           NOT NULL,
    price      DECIMAL(10, 2)           NOT NULL,
    status     VARCHAR(20)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Grant privileges to order_service user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO order_service;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO order_service;