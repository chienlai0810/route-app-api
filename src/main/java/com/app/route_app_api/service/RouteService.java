package com.app.route_app_api.service;

import com.app.route_app_api.dto.RouteRequest;
import com.app.route_app_api.dto.RouteResponse;
import com.app.route_app_api.entity.Route;
import com.app.route_app_api.exception.BusinessRuleException;
import com.app.route_app_api.exception.DuplicateResourceException;
import com.app.route_app_api.exception.ResourceNotFoundException;
import com.app.route_app_api.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Route Service with GIS and overlap validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final MongoTemplate mongoTemplate;

    // Ngưỡng chồng lấn cho phép (5%)
    private static final double OVERLAP_THRESHOLD_PERCENT = 5.0;

    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        log.info("Creating route with code: {}", request.getCode());

        // Check for duplicate code
        if (routeRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Route with code " + request.getCode() + " already exists");
        }

        // Check for route overlap
        validateRouteOverlap(request.getArea(), null);

        Route route = Route.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .productType(request.getProductType())
                .staffMain(request.getStaffMain())
                .staffSub(request.getStaffSub())
                .area(request.getArea())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Route saved = routeRepository.save(route);
        log.info("Created route with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public RouteResponse getRouteById(String id) {
        log.info("Getting route by id: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));

        return mapToResponse(route);
    }

    public RouteResponse getRouteByCode(String code) {
        log.info("Getting route by code: {}", code);

        Route route = routeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with code: " + code));

        return mapToResponse(route);
    }

    public List<RouteResponse> getAllRoutes() {
        log.info("Getting all routes");

        return routeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByStaff(String staffId) {
        log.info("Getting routes by staff id: {}", staffId);

        // Find routes where staff is either main or sub
        List<Route> routes = routeRepository.findAll().stream()
                .filter(route -> staffId.equals(route.getStaffMain()) || staffId.equals(route.getStaffSub()))
                .collect(Collectors.toList());

        return routes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByType(Route.RouteType type) {
        log.info("Getting routes by type: {}", type);

        return routeRepository.findByType(type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RouteResponse updateRoute(String id, RouteRequest request) {
        log.info("Updating route with id: {}", id);

        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));

        // Check for duplicate code if code is being changed
        if (!route.getCode().equals(request.getCode()) &&
            routeRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Route with code " + request.getCode() + " already exists");
        }

        // Check for route overlap (excluding current route)
        validateRouteOverlap(request.getArea(), id);

        route.setCode(request.getCode());
        route.setName(request.getName());
        route.setType(request.getType());
        route.setProductType(request.getProductType());
        route.setStaffMain(request.getStaffMain());
        route.setStaffSub(request.getStaffSub());
        route.setArea(request.getArea());
        route.setUpdatedAt(LocalDateTime.now());

        Route updated = routeRepository.save(route);
        log.info("Updated route with id: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRoute(String id) {
        log.info("Deleting route with id: {}", id);

        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route not found with id: " + id);
        }

        routeRepository.deleteById(id);
        log.info("Deleted route with id: {}", id);
    }

    /**
     * Validate route overlap with existing routes
     * Throws BusinessRuleException if overlap exceeds threshold
     */
    private void validateRouteOverlap(com.app.route_app_api.model.GeoJsonPolygon newArea, String excludeRouteId) {
        // Get all routes except the one being updated
        Query query = new Query();
        if (excludeRouteId != null) {
            query.addCriteria(Criteria.where("_id").ne(excludeRouteId));
        }

        List<Route> existingRoutes = mongoTemplate.find(query, Route.class);

        for (Route existingRoute : existingRoutes) {
            double overlapPercent = calculateOverlapPercentage(newArea, existingRoute.getArea());

            if (overlapPercent > OVERLAP_THRESHOLD_PERCENT) {
                throw new BusinessRuleException(
                    String.format("Route overlaps %.2f%% with existing route '%s' (code: %s). Maximum allowed is %.2f%%",
                        overlapPercent, existingRoute.getName(), existingRoute.getCode(), OVERLAP_THRESHOLD_PERCENT)
                );
            }
        }
    }

    /**
     * Calculate overlap percentage between two polygons
     * This is a simplified calculation - in production, use JTS or similar library
     */
    private double calculateOverlapPercentage(
            com.app.route_app_api.model.GeoJsonPolygon area1,
            com.app.route_app_api.model.GeoJsonPolygon area2) {

        // Simplified overlap detection
        // In production, use JTS (Java Topology Suite) for accurate polygon intersection
        // For now, return 0 to allow creation (or implement basic bounding box check)

        // TODO: Implement proper polygon intersection calculation using JTS
        // Example: Polygon p1 = createPolygon(area1);
        //          Polygon p2 = createPolygon(area2);
        //          double intersectionArea = p1.intersection(p2).getArea();
        //          double overlapPercent = (intersectionArea / p1.getArea()) * 100;

        log.debug("Overlap calculation not fully implemented - using placeholder");
        return 0.0; // Placeholder - implement with JTS for production
    }

    private RouteResponse mapToResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .code(route.getCode())
                .name(route.getName())
                .type(route.getType())
                .productType(route.getProductType())
                .staffMain(route.getStaffMain())
                .staffSub(route.getStaffSub())
                .area(route.getArea())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }
}

