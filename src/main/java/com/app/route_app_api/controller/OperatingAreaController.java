package com.app.route_app_api.controller;

import com.app.route_app_api.dto.ApiResponse;
import com.app.route_app_api.dto.OperatingAreaRequest;
import com.app.route_app_api.dto.OperatingAreaResponse;
import com.app.route_app_api.dto.OperatingAreaStatusResponse;
import com.app.route_app_api.service.OperatingAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Operating Area REST Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/operating-areas")
@RequiredArgsConstructor
public class OperatingAreaController {

    private final OperatingAreaService operatingAreaService;

    @PostMapping
    public ResponseEntity<ApiResponse<OperatingAreaResponse>> createOperatingArea(
            @Valid @RequestBody OperatingAreaRequest request) {
        log.info("POST /api/v1/operating-areas - Creating operating area: {}", request.getName());

        OperatingAreaResponse response = operatingAreaService.createOperatingArea(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Operating area created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OperatingAreaResponse>> getOperatingAreaById(@PathVariable String id) {
        log.info("GET /api/v1/operating-areas/{} - Getting operating area by id", id);

        OperatingAreaResponse response = operatingAreaService.getOperatingAreaById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OperatingAreaResponse>>> getAllOperatingAreas(
            @RequestParam(required = false) String postOfficeId,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String operatingAreaId) {
        log.info("GET /api/v1/operating-areas - Getting operating areas with filters: postOfficeId={}, productType={}, operatingAreaId={}",
                postOfficeId, productType, operatingAreaId);

        List<OperatingAreaResponse> response = operatingAreaService.getOperatingAreas(postOfficeId, productType, operatingAreaId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OperatingAreaResponse>> updateOperatingArea(
            @PathVariable String id,
            @Valid @RequestBody OperatingAreaRequest request) {
        log.info("PUT /api/v1/operating-areas/{} - Updating operating area", id);

        OperatingAreaResponse response = operatingAreaService.updateOperatingArea(id, request);
        return ResponseEntity.ok(ApiResponse.success("Operating area updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOperatingArea(@PathVariable String id) {
        log.info("DELETE /api/v1/operating-areas/{} - Deleting operating area", id);

        operatingAreaService.deleteOperatingArea(id);
        return ResponseEntity.ok(ApiResponse.success("Operating area deleted successfully", null));
    }

    /**
     * Get operating area status - includes information about routes and permissions
     * Used by UI to determine if delete/update operations should be enabled
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OperatingAreaStatusResponse>> getOperatingAreaStatus(@PathVariable String id) {
        log.info("GET /api/v1/operating-areas/{}/status - Getting operating area status", id);

        OperatingAreaStatusResponse response = operatingAreaService.getOperatingAreaStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Check if operating area has routes
     * Simple boolean check for quick validation
     */
    @GetMapping("/{id}/has-routes")
    public ResponseEntity<ApiResponse<Boolean>> hasRoutes(@PathVariable String id) {
        log.info("GET /api/v1/operating-areas/{}/has-routes - Checking if operating area has routes", id);

        boolean hasRoutes = operatingAreaService.hasRoutes(id);
        return ResponseEntity.ok(ApiResponse.success(hasRoutes));
    }

    /**
     * Get route count for operating area
     */
    @GetMapping("/{id}/route-count")
    public ResponseEntity<ApiResponse<Long>> getRouteCount(@PathVariable String id) {
        log.info("GET /api/v1/operating-areas/{}/route-count - Getting route count", id);

        long count = operatingAreaService.getRouteCount(id);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

