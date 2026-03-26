-- Create multiple databases for microservices
CREATE DATABASE portfolio;
CREATE DATABASE marketdata;
CREATE DATABASE keycloak;

-- Create users if needed (using default 'postgres' user for simplicity in this minimal setup is fine, 
-- or create separate users as before but in one script)
CREATE USER keycloak WITH PASSWORD 'keycloak_pass';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
ALTER DATABASE keycloak OWNER TO keycloak;

CREATE USER "user" WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE portfolio TO "user";
ALTER DATABASE portfolio OWNER TO "user";

GRANT ALL PRIVILEGES ON DATABASE marketdata TO "user";
ALTER DATABASE marketdata OWNER TO "user";
