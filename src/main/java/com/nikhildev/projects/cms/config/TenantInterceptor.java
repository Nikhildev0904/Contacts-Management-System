package com.cognitree.internship.cms.config;

import com.cognitree.internship.cms.models.Tenant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);

    private TenantContext tenantContext;

    @Autowired
    public void setTenantContext(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        if (requestPath.equals("/error")) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            logger.error("No authenticated user for path: {}", requestPath);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Authentication required");
            return false;
        }
        Tenant tenant = (Tenant) authentication.getPrincipal();
        if (requestPath.startsWith("/admin/")) {
            if (!"ADMIN".equalsIgnoreCase(tenant.getRole())) {
                logger.error("Non-admin user attempted to access admin path: {}", requestPath);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.getWriter().write("Admin access required");
                return false;
            }
            return true;
        }
        logger.debug("Setting tenant context to tenant ID: {} for user: {}",
                tenant.getId(), tenant.getUsername());
        tenantContext.setTenantId(tenant.getId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        logger.debug("Clearing tenant context");
        tenantContext.clear();
    }
}