-- Grant permissions on specific tables
GRANT SELECT, INSERT, UPDATE, DELETE ON app.document_jobs TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON app.documents TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON app.tenants TO app_user;

-- Grant permissions on all tables in app schema (covers any we missed)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA app TO app_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA app TO app_user;

-- Set default privileges for future tables created by liquibase_user
ALTER DEFAULT PRIVILEGES FOR ROLE liquibase_user IN SCHEMA app 
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user; 