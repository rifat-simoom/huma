-- Create databases
CREATE DATABASE hrm_db;
CREATE DATABASE keycloak_db;

-- Create users
CREATE USER hrm_user WITH ENCRYPTED PASSWORD 'hrm_password';
CREATE USER keycloak_user WITH ENCRYPTED PASSWORD 'keycloak_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE hrm_db TO hrm_user;
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO keycloak_user;

-- Connect to hrm_db and set up schema
\c hrm_db;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO hrm_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO hrm_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO hrm_user;

-- Connect to keycloak_db and set up schema
\c keycloak_db;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO keycloak_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO keycloak_user;