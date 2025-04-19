-- Create application user with restricted permissions
CREATE USER app_user WITH PASSWORD 'app_user_password';

-- Create Liquibase user with more privileges for schema migrations
CREATE USER liquibase_user WITH PASSWORD 'liquibase_password';

-- Create a template database for tenants
CREATE DATABASE tenant_template;
\c tenant_template

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Basic schema setup
CREATE SCHEMA IF NOT EXISTS app;
CREATE SCHEMA IF NOT EXISTS tenant;

-- Create tables in the app schema
CREATE TABLE app.tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create role based permissions tables
CREATE TABLE app.roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- Insert default roles
INSERT INTO app.roles (name, description) VALUES 
('ADMIN', 'Administrator with full privileges'),
('USER', 'Regular user with limited access');

-- Create users table for service management
CREATE TABLE app.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES app.tenants(id),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id INTEGER NOT NULL REFERENCES app.roles(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, username),
    UNIQUE(tenant_id, email)
);

-- Create tenant schema templates
CREATE SCHEMA IF NOT EXISTS tenant_template;

-- Document processing jobs table
CREATE TABLE tenant_template.document_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_location TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    scheduled_time TIMESTAMP WITH TIME ZONE,
    completed_time TIMESTAMP WITH TIME ZONE
);

-- Processed documents table
CREATE TABLE tenant_template.documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id UUID NOT NULL REFERENCES tenant_template.document_jobs(id),
    filename VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    embedding_vector_id VARCHAR(255),
    status VARCHAR(50) DEFAULT 'PROCESSED',
    md5_hash VARCHAR(32) NOT NULL,
    UNIQUE(file_path, md5_hash)
);

-- Grant permissions to application user
GRANT CONNECT ON DATABASE tenant_template TO app_user;
GRANT USAGE ON SCHEMA app TO app_user;
GRANT USAGE ON SCHEMA tenant_template TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA app TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA tenant_template TO app_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA app TO app_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA tenant_template TO app_user;

-- Grant permissions to liquibase user
GRANT CONNECT ON DATABASE tenant_template TO liquibase_user;
GRANT USAGE, CREATE ON SCHEMA app TO liquibase_user;
GRANT USAGE, CREATE ON SCHEMA tenant_template TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA app TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA tenant_template TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA app TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA tenant_template TO liquibase_user;

-- Connect back to default docloader database
\c docloader

-- Create extensions 
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS app;

-- Create tenant management tables
CREATE TABLE app.tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(255) NOT NULL UNIQUE,
    db_name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Grant appropriate permissions
GRANT CONNECT ON DATABASE docloader TO app_user;
GRANT USAGE ON SCHEMA app TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA app TO app_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA app TO app_user;

GRANT CONNECT ON DATABASE docloader TO liquibase_user;
GRANT USAGE, CREATE ON SCHEMA app TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA app TO liquibase_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA app TO liquibase_user;

-- Create a PL/pgSQL function to create a new tenant database
CREATE OR REPLACE FUNCTION app.create_tenant_database(tenant_name VARCHAR, subdomain VARCHAR)
RETURNS UUID AS $$
DECLARE
    tenant_id UUID;
    db_name VARCHAR;
BEGIN
    -- Generate database name from subdomain
    db_name := 'tenant_' || subdomain;
    
    -- Insert tenant record
    INSERT INTO app.tenants (name, subdomain, db_name)
    VALUES (tenant_name, subdomain, db_name)
    RETURNING id INTO tenant_id;
    
    -- Create the tenant database by cloning the template
    EXECUTE 'CREATE DATABASE ' || quote_ident(db_name) || ' TEMPLATE tenant_template';
    
    -- Grant permissions on the new database
    EXECUTE 'GRANT CONNECT ON DATABASE ' || quote_ident(db_name) || ' TO app_user';
    EXECUTE 'GRANT CONNECT ON DATABASE ' || quote_ident(db_name) || ' TO liquibase_user';
    
    RETURN tenant_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER; 