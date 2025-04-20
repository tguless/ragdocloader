package com.docloader.multitenancy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final String DEFAULT_SCHEMA = "public";

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String tenant) {
        // Convert tenant ID to a PostgreSQL schema-compatible format
        // Replace hyphens with underscores since PostgreSQL doesn't allow hyphens in schema names
        if (tenant != null && tenant.contains("-")) {
            tenant = tenant.replace("-", "_");
        }
        CURRENT_TENANT.set(tenant);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
    
    public static String getDefaultSchema() {
        return DEFAULT_SCHEMA;
    }
} 