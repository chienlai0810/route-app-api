package com.app.route_app_api.service;

import com.app.route_app_api.dto.RouteRequest;
import com.app.route_app_api.dto.RouteResponse;
import com.app.route_app_api.entity.OperatingArea;
import com.app.route_app_api.entity.PostOffice;
import com.app.route_app_api.entity.Route;
import com.app.route_app_api.exception.BusinessRuleException;
import com.app.route_app_api.exception.DuplicateResourceException;
import com.app.route_app_api.exception.ResourceNotFoundException;
import com.app.route_app_api.repository.PostOfficeRepository;
import com.app.route_app_api.repository.RouteRepository;
import com.app.route_app_api.repository.OperatingAreaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
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
    private final PostOfficeRepository postOfficeRepository;
    private final OperatingAreaRepository operatingAreaRepository;
    private final MongoTemplate mongoTemplate;


    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        log.info("Creating route with code: {}", request.getCode());

        // Check for duplicate code
        if (routeRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Route with code " + request.getCode() + " already exists");
        }

        // Validate post office exists
        PostOffice postOffice = postOfficeRepository.findById(request.getPostOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Post office not found with id: " + request.getPostOfficeId()));

        // Validate operating area exists
        OperatingArea operatingArea = operatingAreaRepository.findById(request.getOperatingAreaId())
                .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + request.getOperatingAreaId()));

        // Validate route area is within operating area
        validateRouteWithinOperatingArea(request.getArea(), operatingArea);

        // Check for route overlap
        validateRouteOverlap(request.getArea(), null);

        Route route = Route.builder()
                .code(request.getCode())
                .name(request.getName())
                .postOfficeId(request.getPostOfficeId())
                .operatingAreaId(request.getOperatingAreaId())
                .type(request.getType())
                .productType(request.getProductType())
                .staffMain(request.getStaffMain())
                .staffSub(request.getStaffSub())
                .color(request.getColor())
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

    /**
     * Get routes with combined filters
     * Supports filtering by multiple criteria with AND logic
     */
    public List<RouteResponse> getRoutes(String staffId, String postOfficeId, Route.RouteType type,
                                         String productType, String operatingAreaId) {
        log.info("Getting routes with filters: staffId={}, postOfficeId={}, type={}, productType={}, operatingAreaId={}",
                staffId, postOfficeId, type, productType, operatingAreaId);

        // If all filters are null, return all
        if (staffId == null && postOfficeId == null && type == null &&
                productType == null && operatingAreaId == null) {
            return getAllRoutes();
        }

        // Parse product types if provided
        java.util.Set<String> searchProductTypes = new java.util.HashSet<>();
        if (productType != null && !productType.trim().isEmpty()) {
            searchProductTypes = java.util.Arrays.stream(productType.split(";"))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
            log.info("Parsed search product types: {}", searchProductTypes);
        }

        // Validate post office if provided
        if (postOfficeId != null && !postOfficeRepository.existsById(postOfficeId)) {
            throw new ResourceNotFoundException("Post office not found with id: " + postOfficeId);
        }

        // Validate operating area if provided
        if (operatingAreaId != null && !operatingAreaRepository.existsById(operatingAreaId)) {
            throw new ResourceNotFoundException("Operating area not found with id: " + operatingAreaId);
        }

        final java.util.Set<String> finalSearchProductTypes = searchProductTypes;

        // Apply filters - AND logic (must satisfy all provided filters)
        return routeRepository.findAll().stream()
                .filter(route -> {
                    // Filter by staffId if provided (main or sub)
                    if (staffId != null) {
                        boolean isStaff = staffId.equals(route.getStaffMain()) || staffId.equals(route.getStaffSub());
                        if (!isStaff) {
                            log.debug("Route '{}' filtered out - staff mismatch", route.getCode());
                            return false;
                        }
                    }

                    // Filter by postOfficeId if provided
                    if (postOfficeId != null && !postOfficeId.equals(route.getPostOfficeId())) {
                        log.debug("Route '{}' filtered out - postOfficeId mismatch", route.getCode());
                        return false;
                    }

                    // Filter by type if provided
                    if (type != null && !type.equals(route.getType())) {
                        log.debug("Route '{}' filtered out - type mismatch", route.getCode());
                        return false;
                    }

                    // Filter by productType if provided
                    if (!finalSearchProductTypes.isEmpty()) {
                        if (route.getProductType() == null) {
                            log.debug("Route '{}' filtered out - no product type", route.getCode());
                            return false;
                        }

                        java.util.Set<String> routeProductTypes = java.util.Arrays.stream(route.getProductType().split(";"))
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toSet());

                        // Check if there's any intersection
                        boolean hasMatch = false;
                        for (String searchType : finalSearchProductTypes) {
                            if (routeProductTypes.contains(searchType)) {
                                hasMatch = true;
                                break;
                            }
                        }

                        if (!hasMatch) {
                            log.debug("Route '{}' filtered out - no matching product type. Route has {}, searching for {}",
                                    route.getCode(), routeProductTypes, finalSearchProductTypes);
                            return false;
                        }
                    }

                    // Filter by operatingAreaId if provided
                    if (operatingAreaId != null && !operatingAreaId.equals(route.getOperatingAreaId())) {
                        log.debug("Route '{}' filtered out - operatingAreaId mismatch", route.getCode());
                        return false;
                    }

                    log.debug("✅ Route '{}' passes all filters", route.getCode());
                    return true;
                })
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

    public List<RouteResponse> getRoutesByPostOfficeId(String postOfficeId) {
        log.info("Getting routes by post office id: {}", postOfficeId);

        // Validate post office exists
        if (!postOfficeRepository.existsById(postOfficeId)) {
            throw new ResourceNotFoundException("Post office not found with id: " + postOfficeId);
        }

        List<Route> routes = routeRepository.findAll().stream()
                .filter(route -> postOfficeId.equals(route.getPostOfficeId()))
                .collect(Collectors.toList());

        return routes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByProductType(String productType) {
        log.info("Getting routes by product type: {}", productType);

        // Find routes where productType contains the specified type (handle semicolon-separated values)
        List<Route> routes = routeRepository.findAll().stream()
                .filter(route -> {
                    if (route.getProductType() == null) {
                        return false;
                    }
                    // Check if productType contains the specified type
                    // Handle cases like "HH", "HH;KH", "KH;HH;TH"
                    String[] types = route.getProductType().split(";");
                    for (String type : types) {
                        if (type.trim().equalsIgnoreCase(productType.trim())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

        return routes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RouteResponse> getRoutesByOperatingAreaId(String operatingAreaId) {
        log.info("Getting routes by operating area id: {}", operatingAreaId);

        // This would require a relationship between Route and OperatingArea
        // For now, we'll use a simpler approach: find routes that have the same postOfficeId
        // and whose area intersects with the operating area

        // First, get the operating area
        Query query = new Query(Criteria.where("_id").is(operatingAreaId));
        var operatingArea = mongoTemplate.findOne(query, com.app.route_app_api.entity.OperatingArea.class);

        if (operatingArea == null) {
            throw new ResourceNotFoundException("Operating area not found with id: " + operatingAreaId);
        }

        // Find routes that intersect with the operating area's polygon
        Query routeQuery = new Query(
                Criteria.where("area").intersects(operatingArea.getArea())
        );

        List<Route> routes = mongoTemplate.find(routeQuery, Route.class);

        return routes.stream()
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

        // Validate post office exists
        PostOffice postOffice = postOfficeRepository.findById(request.getPostOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Post office not found with id: " + request.getPostOfficeId()));

        // Validate operating area exists
        OperatingArea operatingArea = operatingAreaRepository.findById(request.getOperatingAreaId())
                .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + request.getOperatingAreaId()));

        // Validate route area is within operating area
        validateRouteWithinOperatingArea(request.getArea(), operatingArea);

        // Check for route overlap (excluding current route)
        validateRouteOverlap(request.getArea(), id);

        route.setCode(request.getCode());
        route.setName(request.getName());
        route.setPostOfficeId(request.getPostOfficeId());
        route.setOperatingAreaId(request.getOperatingAreaId());
        route.setType(request.getType());
        route.setProductType(request.getProductType());
        route.setStaffMain(request.getStaffMain());
        route.setStaffSub(request.getStaffSub());
        route.setColor(request.getColor());
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
     * Throws BusinessRuleException if routes have actual area overlap
     * (ignores shared edges or vertices only)
     */
    private void validateRouteOverlap(org.springframework.data.mongodb.core.geo.GeoJsonPolygon newArea, String excludeRouteId) {
        log.debug("Validating route overlap");

        // Get all routes except the one being updated
        Query query = new Query();
        if (excludeRouteId != null) {
            query.addCriteria(Criteria.where("_id").ne(excludeRouteId));
        }

        List<Route> existingRoutes = mongoTemplate.find(query, Route.class);

        for (Route existingRoute : existingRoutes) {
            log.debug("Checking overlap with route: {} (code: {})", existingRoute.getName(), existingRoute.getCode());

            // Check if polygons have actual area overlap using JTS
            if (checkPolygonOverlap(newArea, existingRoute.getArea())) {
                throw new BusinessRuleException(
                        String.format("Route overlaps with existing route '%s' (code: %s). " +
                                        "Two routes cannot have overlapping areas.",
                                existingRoute.getName(), existingRoute.getCode())
                );
            }
        }

        log.debug("No overlapping routes found");
    }

    /**
     * Validate that route area is completely within operating area
     * Allows routes that touch the boundary (edges or vertices) of operating area
     * Uses threshold 1e-6 to handle floating point precision errors
     * Throws BusinessRuleException if route extends outside operating area
     */
    private void validateRouteWithinOperatingArea(
            org.springframework.data.mongodb.core.geo.GeoJsonPolygon routeArea,
            OperatingArea operatingArea) {

        log.debug("Validating route is within operating area: {}", operatingArea.getName());

        try {
            GeometryFactory geometryFactory = new GeometryFactory();

            Polygon routePolygon = convertToJTSPolygon(routeArea, geometryFactory);
            Polygon operatingAreaPolygon = convertToJTSPolygon(operatingArea.getArea(), geometryFactory);

            double THRESHOLD = 1e-6; // Threshold for floating point precision errors

            // First check: Use coveredBy() for standard case
            if (routePolygon.coveredBy(operatingAreaPolygon)) {
                log.debug("Route is within operating area boundaries (coveredBy check passed)");
                return;
            }

            // Second check: Calculate the area that extends outside the operating area
            // and check if it's negligible (within threshold)
            Geometry difference = routePolygon.difference(operatingAreaPolygon);
            double outsideArea = difference.getArea();

            log.debug("Area outside operating area: {} (threshold: {})", outsideArea, THRESHOLD);

            if (outsideArea <= THRESHOLD) {
                log.debug("Route is within operating area boundaries (difference within threshold: {} <= {})",
                        outsideArea, THRESHOLD);
                return;
            }

            // If we get here, the route extends significantly outside the operating area
            throw new BusinessRuleException(
                    String.format("Route area must be completely within operating area '%s'. " +
                                    "The route extends outside the operating area boundaries by area: %.9f",
                            operatingArea.getName(), outsideArea)
            );

        } catch (BusinessRuleException e) {
            throw e; // Re-throw business rule exceptions
        } catch (Exception e) {
            log.error("Error validating route within operating area", e);
            throw new BusinessRuleException(
                    "Failed to validate route area against operating area: " + e.getMessage()
            );
        }
    }

    /**
     * Check if two polygons have actual area overlap using JTS
     * Returns true only if polygons have intersecting area (not just touching edges or vertices)
     */
    private boolean checkPolygonOverlap(
            org.springframework.data.mongodb.core.geo.GeoJsonPolygon polygon1,
            org.springframework.data.mongodb.core.geo.GeoJsonPolygon polygon2) {

        try {
            GeometryFactory geometryFactory = new GeometryFactory();

            Polygon jtsPolygon1 = convertToJTSPolygon(polygon1, geometryFactory);
            Polygon jtsPolygon2 = convertToJTSPolygon(polygon2, geometryFactory);

            // If polygons only touch at boundary (shared edge or vertex), not overlap
            if (jtsPolygon1.touches(jtsPolygon2)) {
                log.debug("Polygons only touch at boundary (shared edge or vertex) - no overlap");
                return false;
            }

            // Calculate intersection area
            Geometry intersection = jtsPolygon1.intersection(jtsPolygon2);
            double overlapArea = intersection.getArea();

            double THRESHOLD = 1e-6; // Threshold for negligible area

            log.debug("Overlap area: {}", overlapArea);

            if (overlapArea > THRESHOLD) {
                log.debug("Polygons overlap with significant area");
                return true;
            }

            // Check if one polygon contains another
            if (jtsPolygon1.contains(jtsPolygon2) || jtsPolygon2.contains(jtsPolygon1)) {
                log.debug("One polygon contains another");
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("Error checking polygon overlap with JTS", e);
            return false;
        }
    }

    /**
     * Convert Spring Data MongoDB GeoJsonPolygon to JTS Polygon
     */
    private Polygon convertToJTSPolygon(
            org.springframework.data.mongodb.core.geo.GeoJsonPolygon geoJsonPolygon,
            GeometryFactory geometryFactory) {

        List<org.springframework.data.geo.Point> points = geoJsonPolygon.getPoints();

        // Convert to JTS Coordinates
        Coordinate[] coordinates = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            org.springframework.data.geo.Point point = points.get(i);
            coordinates[i] = new Coordinate(point.getX(), point.getY());
        }

        // Create JTS Polygon
        return geometryFactory.createPolygon(coordinates);
    }

    private RouteResponse mapToResponse(Route route) {
        String postOfficeName = null;
        if (route.getPostOfficeId() != null) {
            postOfficeName = postOfficeRepository.findById(route.getPostOfficeId())
                    .map(PostOffice::getName)
                    .orElse(null);
        }

        String operatingAreaName = null;
        if (route.getOperatingAreaId() != null) {
            operatingAreaName = operatingAreaRepository.findById(route.getOperatingAreaId())
                    .map(OperatingArea::getName)
                    .orElse(null);
        }

        return RouteResponse.builder()
                .id(route.getId())
                .code(route.getCode())
                .name(route.getName())
                .postOfficeId(route.getPostOfficeId())
                .postOfficeName(postOfficeName)
                .operatingAreaId(route.getOperatingAreaId())
                .operatingAreaName(operatingAreaName)
                .type(route.getType())
                .productType(route.getProductType())
                .staffMain(route.getStaffMain())
                .staffSub(route.getStaffSub())
                .color(route.getColor())
                .area(route.getArea())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }
}
