package com.cognitree.internship.cms.config;

import org.springframework.stereotype.Component;


@Component
public class TenantContext {

    private static final ThreadLocal<String> currentTenant = ThreadLocal.withInitial(() -> null);

    public void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    public String getTenantId() {
        return currentTenant.get();
    }

    public void clear() {
        currentTenant.remove();
    }
}