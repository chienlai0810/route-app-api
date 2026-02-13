package com.app.route_app_api.controller;

import com.app.route_app_api.dto.ApiResponse;
import com.app.route_app_api.dto.RouteRequest;
import com.app.route_app_api.dto.RouteResponse;
import com.app.route_app_api.entity.Route;
import com.app.route_app_api.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Route REST Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(
            @Valid @RequestBody RouteRequest request) {
        log.info("POST /api/v1/routes - Creating route: {}", request.getCode());

        RouteResponse response = routeService.createRoute(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Route created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteById(@PathVariable String id) {
        log.info("GET /api/v1/routes/{} - Getting route by id", id);

        RouteResponse response = routeService.getRouteById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<RouteResponse>> getRouteByCode(@PathVariable String code) {
        log.info("GET /api/v1/routes/code/{} - Getting route by code", code);

        RouteResponse response = routeService.getRouteByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RouteResponse>>> getAllRoutes(
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) Route.RouteType type) {
        log.info("GET /api/v1/routes - Getting routes with filters: staffId={}, type={}", staffId, type);

        List<RouteResponse> response;

        if (staffId != null) {
            response = routeService.getRoutesByStaff(staffId);
        } else if (type != null) {
            response = routeService.getRoutesByType(type);
        } else {
            response = routeService.getAllRoutes();
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(
            @PathVariable String id,
            @Valid @RequestBody RouteRequest request) {
        log.info("PUT /api/v1/routes/{} - Updating route", id);

        RouteResponse response = routeService.updateRoute(id, request);
        return ResponseEntity.ok(ApiResponse.success("Route updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable String id) {
        log.info("DELETE /api/v1/routes/{} - Deleting route", id);

        routeService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.success("Route deleted successfully", null));
    }
}

