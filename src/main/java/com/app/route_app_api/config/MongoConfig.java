package com.app.route_app_api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * MongoDB Configuration
 */
@Slf4j
@Configuration
public class MongoConfig {

    /**
     * Configure MongoTemplate with custom settings
     * Remove _class field from documents
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoMappingContext context) {
        try {
            MappingMongoConverter converter = new MappingMongoConverter(
                    new DefaultDbRefResolver(mongoDbFactory), context);

            // Remove _class field from MongoDB documents
            converter.setTypeMapper(new DefaultMongoTypeMapper(null));

            converter.afterPropertiesSet();

            log.info("MongoTemplate configured successfully");
            return new MongoTemplate(mongoDbFactory, converter);
        } catch (Exception e) {
            log.error("Failed to configure MongoTemplate: {}", e.getMessage());
            throw new RuntimeException("MongoDB configuration failed", e);
        }
    }
}


