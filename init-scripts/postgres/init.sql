-- Create the app schema
CREATE SCHEMA IF NOT EXISTS app;

-- Create the liquibase user with appropriate privileges
CREATE USER liquibase_user WITH PASSWORD 'liquibase_password';

-- Give liquibase_user superuser privileges to ensure it can do anything needed for migrations
ALTER USER liquibase_user WITH SUPERUSER;

-- Grant all privileges on the database
GRANT ALL PRIVILEGES ON DATABASE docloader TO liquibase_user;
GRANT ALL PRIVILEGES ON SCHEMA app TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA app TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA app TO liquibase_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA app GRANT ALL PRIVILEGES ON TABLES TO liquibase_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA app GRANT ALL PRIVILEGES ON SEQUENCES TO liquibase_user;

-- Create the application user with appropriate privileges for running the application
CREATE USER app_user WITH PASSWORD 'app_user_password';
GRANT CONNECT ON DATABASE docloader TO app_user;
GRANT USAGE ON SCHEMA app TO app_user;
-- Grant ALL privileges instead of just SELECT, INSERT, UPDATE, DELETE
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA app TO app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA app TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA app GRANT ALL PRIVILEGES ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA app GRANT ALL PRIVILEGES ON SEQUENCES TO app_user;

-- Also grant privileges on public schema for Quartz tables
GRANT USAGE ON SCHEMA public TO app_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO app_user; 