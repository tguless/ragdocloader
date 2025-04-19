package com.docloader.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private static final long serialVersionUID = 1L;
    
    private final DataSource defaultDataSource;
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    @Autowired
    public TenantConnectionProvider(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return defaultDataSource;
    }

    @Override
    protected DataSource selectDataSource(Object tenantIdentifier) {
        if (tenantIdentifier == null) {
            return defaultDataSource;
        }
        
        String tenant = tenantIdentifier.toString();
        if (tenant.trim().isEmpty()) {
            return defaultDataSource;
        }
        
        return tenantDataSources.computeIfAbsent(tenant, this::createDataSourceForTenant);
    }
    
    private DataSource createDataSourceForTenant(String tenant) {
        try {
            log.info("Creating new datasource for tenant: {}", tenant);
            
            // Retrieve connection info from default DB to connect to tenant DB
            try (Connection connection = defaultDataSource.getConnection()) {
                // We'll use the same user credentials and simply change the database name
                String url = connection.getMetaData().getURL();
                // Modify URL to point to tenant database
                String tenantUrl = url.substring(0, url.lastIndexOf("/") + 1) + tenant;
                
                // Create new datasource for tenant
                org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = 
                        new org.springframework.jdbc.datasource.DriverManagerDataSource();
                dataSource.setUrl(tenantUrl);
                dataSource.setUsername(connection.getMetaData().getUserName());
                
                // We need to set the password from application properties - we can't get it from metadata
                // For now, using the same password for all tenant connections
                dataSource.setPassword("app_user_password");
                
                return dataSource;
            }
        } catch (SQLException e) {
            log.error("Error creating datasource for tenant {}", tenant, e);
            throw new RuntimeException("Could not create tenant datasource", e);
        }
    }
} 