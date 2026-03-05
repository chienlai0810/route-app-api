package com.app.route_app_api.controller;

import com.app.route_app_api.dto.ApiResponse;
import com.app.route_app_api.service.DataSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller for data management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DataSeedService dataSeedService;

    @PostMapping("/seed-data")
    public ResponseEntity<ApiResponse<String>> seedData(
            @RequestParam(defaultValue = "false") boolean force) {
        log.info("POST /api/v1/admin/seed-data - force={}", force);

        String result = dataSeedService.seedData(force);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @DeleteMapping("/clear-data")
    public ResponseEntity<ApiResponse<String>> clearData() {
        log.info("DELETE /api/v1/admin/clear-data - Clearing all data");

        dataSeedService.clearAllData();
        return ResponseEntity.ok(ApiResponse.success("All data cleared successfully", null));
    }

    @DeleteMapping("/clear-routes")
    public ResponseEntity<ApiResponse<String>> clearRoutes() {
        log.info("DELETE /api/v1/admin/clear-routes - Clearing all routes");

        dataSeedService.clearRoutes();
        return ResponseEntity.ok(ApiResponse.success("All routes cleared successfully", null));
    }
}

