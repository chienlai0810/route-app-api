package com.app.route_app_api.util;

import com.app.route_app_api.repository.RouteRepository;
import com.app.route_app_api.service.DataSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data Seeder to populate database with sample routes on application startup
 * Chỉ chạy khi database chưa có routes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RouteRepository routeRepository;
    private final DataSeedService dataSeedService;

    @Override
    public void run(String... args) {
        log.info("Checking if route seeding is needed...");

        // Check if routes already exist
        if (routeRepository.count() > 0) {
            log.info("Database already contains routes. Skipping automatic seeding.");
            log.info("To seed routes manually, use: POST /api/v1/admin/seed-data?force=true");
            return;
        }

        try {
            log.info("Starting automatic route seeding...");
            String result = dataSeedService.seedData(false);
            log.info("Route seeding completed: {}", result);
        } catch (Exception e) {
            log.error("Error during route seeding", e);
        }
    }
}

