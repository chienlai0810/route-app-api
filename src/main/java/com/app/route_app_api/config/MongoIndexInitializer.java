package com.app.route_app_api.config;

import com.app.route_app_api.entity.PostOffice;
import com.app.route_app_api.entity.Route;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

/**
 * Initialize MongoDB indexes at application startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void initIndexes() {
        try {
            log.info("Initializing MongoDB indexes...");

            // Check MongoDB connection first
            mongoTemplate.getDb().getName();
            log.info("MongoDB connection verified");

            // PostOffice indexes
            createPostOfficeIndexes();

            // Route indexes
            createRouteIndexes();

            log.info("MongoDB indexes initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MongoDB indexes: {}. Application will continue but indexes may not be created.",
                    e.getMessage());
            log.debug("Full stack trace: ", e);
        }
    }

    private void createPostOfficeIndexes() {
        try {
            var indexOps = mongoTemplate.indexOps(PostOffice.class);

            // Drop all indexes except _id (to avoid conflicts)
            indexOps.getIndexInfo().forEach(indexInfo -> {
                String indexName = indexInfo.getName();
                if (!indexName.equals("_id_")) {
                    try {
                        indexOps.dropIndex(indexName);
                        log.debug("Dropped existing index: {}", indexName);
                    } catch (Exception e) {
                        log.warn("Could not drop index {}: {}", indexName, e.getMessage());
                    }
                }
            });

            // Create unique index on code
            indexOps.ensureIndex(new Index().on("code", Sort.Direction.ASC).unique().named("code_unique"));

            // NOTE: 2dsphere index on location field is removed to prevent index build errors
            // You can create it manually in MongoDB if needed: db.post_offices.createIndex({location: "2dsphere"})
            log.info("PostOffice indexes created successfully (location index not created)");

        } catch (Exception e) {
            log.error("Failed to create PostOffice indexes: {}", e.getMessage());
            throw e;
        }
    }

    private void createRouteIndexes() {
        try {
            var indexOps = mongoTemplate.indexOps(Route.class);

            // Drop all indexes except _id (to avoid conflicts)
            indexOps.getIndexInfo().forEach(indexInfo -> {
                String indexName = indexInfo.getName();
                if (!indexName.equals("_id_")) {
                    try {
                        indexOps.dropIndex(indexName);
                        log.debug("Dropped existing index: {}", indexName);
                    } catch (Exception e) {
                        log.warn("Could not drop index {}: {}", indexName, e.getMessage());
                    }
                }
            });

            // Create unique index on code
            indexOps.ensureIndex(new Index().on("code", Sort.Direction.ASC).unique().named("code_unique"));

//            // Create index on postOfficeId for faster lookups
//            indexOps.ensureIndex(new Index().on("postOfficeId", Sort.Direction.ASC).named("postOfficeId_index"));
//
//            // Create 2dsphere index on area for geospatial queries (Point-in-Polygon)
//            indexOps.ensureIndex(new GeospatialIndex("area").named("area_2dsphere"));

            log.info("Route indexes created successfully");
        } catch (Exception e) {
            log.error("Failed to create Route indexes: {}", e.getMessage());
            throw e;
        }
    }
}

