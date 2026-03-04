package com.app.route_app_api.controller;

import com.app.route_app_api.dto.ApiResponse;
import com.app.route_app_api.dto.PointInPolygonRequest;
import com.app.route_app_api.dto.PointInPolygonResponse;
import com.app.route_app_api.service.GisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * GIS REST Controller for spatial queries
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/gis")
@RequiredArgsConstructor
public class GisController {

    private final GisService gisService;

    @PostMapping("/check-point")
    public ResponseEntity<ApiResponse<PointInPolygonResponse>> checkPointInPolygon(
            @Valid @RequestBody PointInPolygonRequest request) {
        log.info("POST /api/v1/gis/check-point - Checking point ({}, {}) with productType: {}",
                request.getLatitude(), request.getLongitude(), request.getProductType());

        PointInPolygonResponse response = gisService.checkPointInPolygon(
                request.getLatitude(),
                request.getLongitude(),
                request.getProductType()
        );

        String message = response.isFound()
                ? "Point is within " + response.getMatchingRoutes().size() + " route(s)"
                : "Point is not within any route";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/check-point")
    public ResponseEntity<ApiResponse<PointInPolygonResponse>> checkPointInPolygonGet(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String productType) {
        log.info("GET /api/v1/gis/check-point - Checking point ({}, {}) with productType: {}",
                latitude, longitude, productType);

        PointInPolygonResponse response = gisService.checkPointInPolygon(latitude, longitude, productType);

        String message = response.isFound()
                ? "Point is within " + response.getMatchingRoutes().size() + " route(s)"
                : "Point is not within any route";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}

