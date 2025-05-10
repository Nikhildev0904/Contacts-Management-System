package com.cognitree.internship.cms.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.lang.NonNull;

@Configuration
public class MultiTenantMongoConfig {

    private static final Logger logger = LoggerFactory.getLogger(MultiTenantMongoConfig.class);
    private static final String DEFAULT_DB = "default";

    private final TenantContext tenantContext;

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;

    @Value("${spring.data.mongodb.port:27017}")
    private int port;

    @Autowired
    public MultiTenantMongoConfig(TenantContext tenantContext) {
        this.tenantContext = tenantContext;
    }

    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format("mongodb://%s:%d", host, port);
        logger.debug("Connecting to MongoDB at: {}", connectionString);
        return MongoClients.create(connectionString);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new MultiTenantMongoDBFactory(mongoClient, tenantContext);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

    /**
     * Custom MongoDB factory that selects the database based on the current tenant
     */
    private static class MultiTenantMongoDBFactory extends SimpleMongoClientDatabaseFactory {

        private final TenantContext tenantContext;

        public MultiTenantMongoDBFactory(MongoClient mongoClient, TenantContext tenantContext) {
            super(mongoClient, DEFAULT_DB);
            this.tenantContext = tenantContext;
        }

        @Override
        @NonNull
        protected String getDefaultDatabaseName() {
            String tenantId = tenantContext.getTenantId();
            if (tenantId != null) {
                String dbName = "tenant_" + tenantId;
                logger.debug("Using tenant database: {}", dbName);
                return dbName;
            }
            logger.debug("No tenant context found, using default database");
            return DEFAULT_DB;
        }
    }
}
