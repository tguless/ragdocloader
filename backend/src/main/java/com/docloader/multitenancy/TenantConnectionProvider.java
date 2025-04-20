package com.docloader.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider {

    private static final long serialVersionUID = 1L;
    
    private final DataSource dataSource;

    @Autowired
    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        log.info("Getting connection for tenant: {}", tenantIdentifier);
        
        final Connection connection = getAnyConnection();
        
        try {
            // Set the schema for this connection
            // Replace hyphens with underscores since PostgreSQL doesn't allow hyphens in schema names
            String schema = tenantIdentifier.toString().replace("-", "_");
            connection.createStatement().execute(String.format("SET search_path TO %s", schema));
            
            log.info("Set search_path to schema: {}", schema);
        }
        catch (SQLException e) {
            log.error("Error setting search_path for tenant {}: {}", tenantIdentifier, e.getMessage());
            connection.close();
            throw e;
        }
        
        return connection;
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        try {
            // Reset search path to avoid leaking tenant context
            connection.createStatement().execute("SET search_path TO public");
            log.debug("Reset search_path to public for tenant: {}", tenantIdentifier);
        }
        catch (SQLException e) {
            log.error("Error resetting search_path for tenant {}: {}", tenantIdentifier, e.getMessage());
        }
        
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Unwrapping not supported");
    }
} 