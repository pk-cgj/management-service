-- Create Keycloak database and user
CREATE DATABASE keycloak;
CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak_password';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Connect to keycloak database
\c keycloak

-- Create schema and grant permissions
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL ON ALL TABLES IN SCHEMA public TO keycloak;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO keycloak;

-- Set the search path
ALTER DATABASE keycloak SET search_path TO public;

-- Create User Service database and user
CREATE DATABASE user_service;
CREATE USER user_service WITH ENCRYPTED PASSWORD 'user_service_password';
GRANT ALL PRIVILEGES ON DATABASE user_service TO user_service;

-- Create Order Service database and user
CREATE DATABASE order_service;
CREATE USER order_service WITH ENCRYPTED PASSWORD 'order_service_password';
GRANT ALL PRIVILEGES ON DATABASE order_service TO order_service;
