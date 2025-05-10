package com.cognitree.internship.cms.services;

import com.cognitree.internship.cms.exceptions.ResourceAlreadyExistsException;
import com.cognitree.internship.cms.exceptions.ResourceNotFoundException;
import com.cognitree.internship.cms.models.PagedResponse;
import com.cognitree.internship.cms.models.Tenant;
import com.cognitree.internship.cms.repositories.TenantRepository;
import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
    private static final String MASTER_DB = "master";

    private final TenantRepository tenantRepository;
    private final MongoClient mongoClient;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public TenantService(TenantRepository tenantRepository,
                         MongoClient mongoClient,
                         BCryptPasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.mongoClient = mongoClient;
        this.passwordEncoder = passwordEncoder;
    }


    public PagedResponse<Tenant> getAllTenants(String name, int page, int size,
                                               String sortBy, Sort.Direction sortOrder) {
        logger.debug("Fetching tenants with name filter: {}, page: {}", name, page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortOrder, sortBy));
        Page<Tenant> tenantsPage;
        if (name != null && !name.isEmpty()) {
            tenantsPage = tenantRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            tenantsPage = tenantRepository.findAll(pageable);
        }
        return PagedResponse.fromPage(tenantsPage);
    }

    public Tenant getTenantById(String tenantId) {
        logger.debug("Fetching tenant with ID: {}", tenantId);
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    logger.error("Tenant not found with ID: {}", tenantId);
                    return new ResourceNotFoundException("Tenant not found with id: " + tenantId);
                });
    }

    public Tenant createTenant(Tenant tenant) {
        logger.debug("Creating new tenant with name: {}", tenant.getName());
        if (tenantRepository.existsByUsername(tenant.getUsername())) {
            logger.error("Tenant already exists with username: {}", tenant.getUsername());
            throw new ResourceAlreadyExistsException("Username already exists: " + tenant.getUsername());
        }
        tenant.setPassword(passwordEncoder.encode(tenant.getPassword()));
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        Tenant savedTenant = tenantRepository.save(tenant);
        logger.info("Created tenant with ID: {}", savedTenant.getId());
        if (!"ADMIN".equalsIgnoreCase(tenant.getRole())) {
            String dbName = "tenant_" + savedTenant.getId();
            initializeTenantDatabase(dbName);
        }
        return savedTenant;
    }

    public Tenant updateTenant(String tenantId, Tenant tenantDetails) {
        logger.debug("Updating tenant with ID: {}", tenantId);
        Tenant existingTenant = getTenantById(tenantId);
        // Check name uniqueness if changed
        if (tenantDetails.getName() != null && !tenantDetails.getName().equals(existingTenant.getName())) {
            if (tenantRepository.existsByNameIgnoreCase(tenantDetails.getName())) {
                logger.error("Cannot update tenant. Name already exists: {}", tenantDetails.getName());
                throw new ResourceAlreadyExistsException("Tenant with name: " + tenantDetails.getName() + " already exists");
            }
            existingTenant.setName(tenantDetails.getName());
        }
        if (tenantDetails.getDescription() != null) {
            existingTenant.setDescription(tenantDetails.getDescription());
        }
        existingTenant.setUpdatedAt(LocalDateTime.now());
        Tenant updatedTenant = tenantRepository.save(existingTenant);
        logger.info("Updated tenant with ID: {}", tenantId);
        return updatedTenant;
    }

    public void deleteTenant(String tenantId) {
        logger.debug("Attempting to delete tenant with ID: {}", tenantId);
        Tenant tenant = getTenantById(tenantId);
        String dbName = "tenant_" + tenantId;
        dropTenantDatabase(dbName);
        tenantRepository.delete(tenant);
        logger.info("Deleted tenant with ID: {}", tenantId);
    }

    private void initializeTenantDatabase(String dbName) {
        logger.debug("Initializing tenant database: {}", dbName);
        MongoTemplate tenantTemplate = new MongoTemplate(mongoClient, dbName);
        if (!tenantTemplate.collectionExists("contacts")) {
            tenantTemplate.createCollection("contacts");
        }
        if (!tenantTemplate.collectionExists("categories")) {
            tenantTemplate.createCollection("categories");
        }
        logger.info("Initialized tenant database: {}", dbName);
    }

    private void dropTenantDatabase(String dbName) {
        logger.debug("Dropping tenant database: {}", dbName);
        mongoClient.getDatabase(dbName).drop();
        logger.info("Dropped tenant database: {}", dbName);
    }
}
