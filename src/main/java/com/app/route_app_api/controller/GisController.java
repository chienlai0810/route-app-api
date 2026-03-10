package com.app.route_app_api.controller;

import com.app.route_app_api.dto.*;
import com.app.route_app_api.service.AddressService;
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
    private final AddressService addressService;

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

    /**
     * Check address and find matching routes (POST)
     */
    @PostMapping("/check-address")
    public ResponseEntity<ApiResponse<AddressCheckResponse>> checkAddress(
            @Valid @RequestBody AddressCheckRequest request) {
        log.info("POST /api/v1/gis/check-address - Checking address: {} with productType: {}",
                request.getAddress(), request.getProductType());

        AddressCheckResponse response = addressService.checkAddress(
                request.getAddress(),
                request.getProductType()
        );

        String message = response.getRouteInfo() != null && response.getRouteInfo().isFound()
                ? "Address found and is within " + response.getRouteInfo().getMatchingRoutes().size() + " route(s)"
                : "Address found but is not within any route";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Check address and find matching routes (GET)
     */
    @GetMapping("/check-address")
    public ResponseEntity<ApiResponse<AddressCheckResponse>> checkAddressGet(
            @RequestParam String address,
            @RequestParam(required = false) String productType) {
        log.info("GET /api/v1/gis/check-address - Checking address: {} with productType: {}",
                address, productType);

        AddressCheckResponse response = addressService.checkAddress(address, productType);

        String message = response.getRouteInfo() != null && response.getRouteInfo().isFound()
                ? "Address found and is within " + response.getRouteInfo().getMatchingRoutes().size() + " route(s)"
                : "Address found but is not within any route";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }
}

