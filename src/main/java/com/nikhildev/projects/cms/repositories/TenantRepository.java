package com.cognitree.internship.cms.repositories;

import com.cognitree.internship.cms.models.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends MongoRepository<Tenant, String> {

    Page<Tenant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    Optional<Tenant> findByUsername(String username);

    boolean existsByUsername(String username);
}