package com.cognitree.internship.cms.controllers;

import com.cognitree.internship.cms.models.PagedResponse;
import com.cognitree.internship.cms.models.Tenant;
import com.cognitree.internship.cms.services.TenantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tenants")
@PreAuthorize("hasRole('ADMIN')")
public class TenantController {

    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    private final TenantService tenantService;

    @Autowired
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Tenant>> getAllTenants(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortOrder) {
        logger.info("Fetching tenants with name filter: {}, page: {}", name, page);
        PagedResponse<Tenant> response = tenantService.getAllTenants(name, page, pageSize, sortBy, sortOrder);
        logger.debug("Fetched {} tenants", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable String tenantId) {
        logger.info("Fetching tenant with ID: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody Tenant tenant) {
        logger.info("Creating new tenant with name: {}", tenant.getName());
        Tenant createdTenant = tenantService.createTenant(tenant);
        logger.info("Created tenant with ID: {}", createdTenant.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<Tenant> updateTenant(
            @PathVariable String tenantId,
            @Valid @RequestBody Tenant tenant) {
        logger.info("Updating tenant with ID: {}", tenantId);
        Tenant updatedTenant = tenantService.updateTenant(tenantId, tenant);
        logger.info("Updated tenant with ID: {}", tenantId);
        return ResponseEntity.ok(updatedTenant);
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String tenantId) {
        logger.info("Deleting tenant with ID: {}", tenantId);
        tenantService.deleteTenant(tenantId);
        logger.info("Deleted tenant with ID: {}", tenantId);
        return ResponseEntity.noContent().build();
    }
}