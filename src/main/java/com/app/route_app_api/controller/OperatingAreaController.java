package com.app.route_app_api.controller;

import com.app.route_app_api.dto.ApiResponse;
import com.app.route_app_api.dto.OperatingAreaRequest;
import com.app.route_app_api.dto.OperatingAreaResponse;
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
}

