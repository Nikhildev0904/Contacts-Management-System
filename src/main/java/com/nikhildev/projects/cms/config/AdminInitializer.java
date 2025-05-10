package com.cognitree.internship.cms.config;

import com.cognitree.internship.cms.models.Tenant;
import com.cognitree.internship.cms.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminInitializer(TenantRepository tenantRepository, PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (tenantRepository.findByUsername("admin").isEmpty()) {
            Tenant admin = new Tenant();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setName("System Administrator");
            admin.setDescription("System administrator account");
            admin.setRole("ADMIN");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            tenantRepository.save(admin);
        }
    }
}