package com.docloader.multitenancy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Value("${docloader.multi-tenancy.tenant-resolver-strategy:SUBDOMAIN}")
    private String tenantResolverStrategy;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenant = resolveTenant(request);
        if (tenant != null) {
            TenantContext.setCurrentTenant(tenant);
            log.debug("Tenant set to: {}", tenant);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Not used
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
        log.debug("Tenant context cleared");
    }

    private String resolveTenant(HttpServletRequest request) {
        if ("SUBDOMAIN".equals(tenantResolverStrategy)) {
            return resolveFromSubdomain(request);
        } else if ("HEADER".equals(tenantResolverStrategy)) {
            return resolveFromHeader(request);
        } else if ("PATH".equals(tenantResolverStrategy)) {
            return resolveFromPath(request);
        }
        return null;
    }

    private String resolveFromSubdomain(HttpServletRequest request) {
        String host = request.getServerName();
        if (host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            // The tenant database prefix is "tenant_" so we add it here
            return "tenant_" + subdomain;
        }
        return null;
    }

    private String resolveFromHeader(HttpServletRequest request) {
        String tenant = request.getHeader("X-TenantID");
        if (tenant != null && !tenant.isEmpty()) {
            return "tenant_" + tenant;
        }
        return null;
    }

    private String resolveFromPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String[] parts = path.split("/");
        if (parts.length > 1) {
            String potentialTenant = parts[1];
            if (!potentialTenant.equals("api") && !potentialTenant.startsWith("$")) {
                return "tenant_" + potentialTenant;
            }
        }
        return null;
    }
} 