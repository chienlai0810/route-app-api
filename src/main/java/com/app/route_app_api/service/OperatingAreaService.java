package com.app.route_app_api.service;

import com.app.route_app_api.dto.OperatingAreaRequest;
import com.app.route_app_api.dto.OperatingAreaResponse;
import com.app.route_app_api.dto.OperatingAreaStatusResponse;
import com.app.route_app_api.entity.OperatingArea;
import com.app.route_app_api.entity.PostOffice;
import com.app.route_app_api.exception.BusinessRuleException;
import com.app.route_app_api.exception.ResourceNotFoundException;
import com.app.route_app_api.repository.OperatingAreaRepository;
import com.app.route_app_api.repository.PostOfficeRepository;
import com.app.route_app_api.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Operating Area Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperatingAreaService {

    private final OperatingAreaRepository operatingAreaRepository;
    private final PostOfficeRepository postOfficeRepository;
    private final RouteRepository routeRepository;
    private final MongoTemplate mongoTemplate;

    @Transactional
    public OperatingAreaResponse createOperatingArea(OperatingAreaRequest request) {
        log.info("Creating operating area: {}", request.getName());

        // Validate post office exists
        if (!postOfficeRepository.existsById(request.getPostOfficeId())) {
            throw new ResourceNotFoundException("Post office not found with id: " + request.getPostOfficeId());
        }

        // Check for overlap with existing operating areas that have overlapping product types
        validateOperatingAreaOverlap(request.getArea(), request.getProductType(), null);

        OperatingArea operatingArea = OperatingArea.builder()
                .name(request.getName())
                .postOfficeId(request.getPostOfficeId())
                .productType(request.getProductType())
                .area(request.getArea())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OperatingArea saved = operatingAreaRepository.save(operatingArea);
        log.info("Created operating area with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public OperatingAreaResponse getOperatingAreaById(String id) {
        log.info("Getting operating area by id: {}", id);

        OperatingArea operatingArea = operatingAreaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + id));

        return mapToResponse(operatingArea);
    }

    public List<OperatingAreaResponse> getAllOperatingAreas() {
        log.info("Getting all operating areas");

        return operatingAreaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get operating areas with combined filters
     * Supports filtering by postOfficeId AND/OR productType AND/OR operatingAreaId
     */
    public List<OperatingAreaResponse> getOperatingAreas(String postOfficeId, String productType, String operatingAreaId) {
        log.info("Getting operating areas with filters: postOfficeId={}, productType={}, operatingAreaId={}",
                postOfficeId, productType, operatingAreaId);

        // If all filters are null, return all
        if (postOfficeId == null && productType == null && operatingAreaId == null) {
            return getAllOperatingAreas();
        }

        // If operatingAreaId is provided, return that specific area
        if (operatingAreaId != null) {
            OperatingArea area = operatingAreaRepository.findById(operatingAreaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + operatingAreaId));

            // Check other filters if provided
            boolean matches = true;

            if (postOfficeId != null && !postOfficeId.equals(area.getPostOfficeId())) {
                log.debug("Area '{}' filtered out - postOfficeId mismatch", area.getName());
                matches = false;
            }

            if (matches && productType != null && !productType.trim().isEmpty()) {
                Set<String> searchProductTypes = parseProductTypes(productType);
                Set<String> areaProductTypes = parseProductTypes(area.getProductType());

                boolean hasMatch = false;
                for (String searchType : searchProductTypes) {
                    if (areaProductTypes.contains(searchType)) {
                        hasMatch = true;
                        break;
                    }
                }

                if (!hasMatch) {
                    log.debug("Area '{}' filtered out - no matching product type", area.getName());
                    matches = false;
                }
            }

            if (matches) {
                log.info("✅ Area '{}' matches all filters", area.getName());
                return List.of(mapToResponse(area));
            } else {
                return List.of();
            }
        }

        // Parse product types if provided
        Set<String> searchProductTypes = new HashSet<>();
        if (productType != null && !productType.trim().isEmpty()) {
            searchProductTypes = parseProductTypes(productType);
            log.info("Parsed search product types: {}", searchProductTypes);
        }

        // Validate post office if provided
        if (postOfficeId != null && !postOfficeRepository.existsById(postOfficeId)) {
            throw new ResourceNotFoundException("Post office not found with id: " + postOfficeId);
        }

        final Set<String> finalSearchProductTypes = searchProductTypes;

        // Apply filters - AND logic (must satisfy all provided filters)
        return operatingAreaRepository.findAll().stream()
                .filter(area -> {
                    // Filter by postOfficeId if provided
                    if (postOfficeId != null && !postOfficeId.equals(area.getPostOfficeId())) {
                        log.debug("Area '{}' filtered out - postOfficeId mismatch", area.getName());
                        return false;
                    }

                    // Filter by productType if provided
                    if (!finalSearchProductTypes.isEmpty()) {
                        Set<String> areaProductTypes = parseProductTypes(area.getProductType());

                        // Check if there's any intersection between search types and area types
                        boolean hasMatch = false;
                        for (String searchType : finalSearchProductTypes) {
                            if (areaProductTypes.contains(searchType)) {
                                hasMatch = true;
                                break;
                            }
                        }

                        if (!hasMatch) {
                            log.debug("Area '{}' filtered out - no matching product type. Area has {}, searching for {}",
                                    area.getName(), areaProductTypes, finalSearchProductTypes);
                            return false;
                        }

                        log.debug("✅ Area '{}' matches product type filter", area.getName());
                    }

                    log.debug("✅ Area '{}' passes all filters", area.getName());
                    return true;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OperatingAreaResponse> getOperatingAreasByPostOfficeId(String postOfficeId) {
        log.info("Getting operating areas by post office id: {}", postOfficeId);

        // Validate post office exists
        if (!postOfficeRepository.existsById(postOfficeId)) {
            throw new ResourceNotFoundException("Post office not found with id: " + postOfficeId);
        }

        return operatingAreaRepository.findByPostOfficeId(postOfficeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OperatingAreaResponse> getOperatingAreasByProductType(String productType) {
        log.info("Getting operating areas by product type: {}", productType);

        // Parse the input product types (e.g., "KH;HH" -> ["KH", "HH"])
        Set<String> searchProductTypes = parseProductTypes(productType);

        if (searchProductTypes.isEmpty()) {
            log.warn("No valid product types provided");
            return List.of();
        }

        log.info("Parsed search product types: {}", searchProductTypes);

        // Get all operating areas and filter by matching product types
        return operatingAreaRepository.findAll().stream()
                .filter(area -> {
                    // Parse product types from the operating area
                    Set<String> areaProductTypes = parseProductTypes(area.getProductType());

                    log.debug("Area '{}' has product types: {} (raw: '{}')",
                            area.getName(), areaProductTypes, area.getProductType());

                    // Check if there's any intersection between search types and area types
                    // Return true if ANY product type matches
                    boolean hasMatch = false;
                    for (String searchType : searchProductTypes) {
                        if (areaProductTypes.contains(searchType)) {
                            log.info("✅ Area '{}' MATCHES - contains product type: {}", area.getName(), searchType);
                            hasMatch = true;
                            break;
                        }
                    }

                    if (!hasMatch) {
                        log.debug("❌ Area '{}' does NOT match - area has {} but searching for {}",
                                area.getName(), areaProductTypes, searchProductTypes);
                    }

                    return hasMatch;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OperatingAreaResponse updateOperatingArea(String id, OperatingAreaRequest request) {
        log.info("Updating operating area with id: {}", id);

        OperatingArea operatingArea = operatingAreaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + id));

        // Validate post office exists
        if (!postOfficeRepository.existsById(request.getPostOfficeId())) {
            throw new ResourceNotFoundException("Post office not found with id: " + request.getPostOfficeId());
        }

        // Check for overlap with existing operating areas (excluding current one)
        validateOperatingAreaOverlap(request.getArea(), request.getProductType(), id);

        operatingArea.setName(request.getName());
        operatingArea.setPostOfficeId(request.getPostOfficeId());
        operatingArea.setProductType(request.getProductType());
        operatingArea.setArea(request.getArea());
        operatingArea.setUpdatedAt(LocalDateTime.now());

        OperatingArea updated = operatingAreaRepository.save(operatingArea);
        log.info("Updated operating area with id: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteOperatingArea(String id) {
        log.info("Deleting operating area with id: {}", id);

        if (!operatingAreaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Operating area not found with id: " + id);
        }

        operatingAreaRepository.deleteById(id);
        log.info("Deleted operating area with id: {}", id);
    }

    /**
     * Check if an operating area has any routes
     * Returns true if there are routes using this operating area
     */
    public boolean hasRoutes(String operatingAreaId) {
        log.info("Checking if operating area {} has routes", operatingAreaId);

        // Validate operating area exists
        if (!operatingAreaRepository.existsById(operatingAreaId)) {
            throw new ResourceNotFoundException("Operating area not found with id: " + operatingAreaId);
        }

        boolean hasRoutes = routeRepository.existsByOperatingAreaId(operatingAreaId);
        log.info("Operating area {} has routes: {}", operatingAreaId, hasRoutes);

        return hasRoutes;
    }

    /**
     * Get count of routes in an operating area
     */
    public long getRouteCount(String operatingAreaId) {
        log.info("Getting route count for operating area {}", operatingAreaId);

        // Validate operating area exists
        if (!operatingAreaRepository.existsById(operatingAreaId)) {
            throw new ResourceNotFoundException("Operating area not found with id: " + operatingAreaId);
        }

        long count = routeRepository.findByOperatingAreaId(operatingAreaId).size();
        log.info("Operating area {} has {} routes", operatingAreaId, count);

        return count;
    }

    /**
     * Get operating area status with information about routes and ability to delete/update
     * This helps UI to enable/disable certain actions
     */
    public OperatingAreaStatusResponse getOperatingAreaStatus(String operatingAreaId) {
        log.info("Getting status for operating area {}", operatingAreaId);

        // Get operating area
        OperatingArea operatingArea = operatingAreaRepository.findById(operatingAreaId)
                .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + operatingAreaId));

        // Check for routes
        boolean hasRoutes = routeRepository.existsByOperatingAreaId(operatingAreaId);
        long routeCount = hasRoutes ? routeRepository.findByOperatingAreaId(operatingAreaId).size() : 0;

        // Determine if can delete/update
        // Usually, if there are routes, deletion might be restricted
        // Update might still be allowed but with restrictions
        boolean canDelete = !hasRoutes;
        boolean canUpdate = !hasRoutes;

        String message;
        if (hasRoutes) {
            message = String.format("Operating area contains %d route(s). Deletion is not allowed. " +
                    "Please remove all routes before deleting this operating area.", routeCount);
        } else {
            message = "Operating area has no routes. All operations are allowed.";
        }

        return OperatingAreaStatusResponse.builder()
                .operatingAreaId(operatingAreaId)
                .operatingAreaName(operatingArea.getName())
                .hasRoutes(hasRoutes)
                .routeCount(routeCount)
                .canDelete(canDelete)
                .canUpdate(canUpdate)
                .message(message)
                .build();
    }

    private OperatingAreaResponse mapToResponse(OperatingArea operatingArea) {
        String postOfficeName = null;
        if (operatingArea.getPostOfficeId() != null) {
            postOfficeName = postOfficeRepository.findById(operatingArea.getPostOfficeId())
                    .map(PostOffice::getName)
                    .orElse(null);
        }

        return OperatingAreaResponse.builder()
                .id(operatingArea.getId())
                .name(operatingArea.getName())
                .postOfficeId(operatingArea.getPostOfficeId())
                .postOfficeName(postOfficeName)
                .productType(operatingArea.getProductType())
                .area(operatingArea.getArea())
                .createdAt(operatingArea.getCreatedAt())
                .updatedAt(operatingArea.getUpdatedAt())
                .build();
    }

    /**
     * Validate operating area overlap with existing areas that have overlapping product types
     * Throws BusinessRuleException if overlap is detected
     */
    private void validateOperatingAreaOverlap(
            org.springframework.data.mongodb.core.geo.GeoJsonPolygon newArea,
            String newProductType,
            String excludeAreaId) {

        log.debug("Validating operating area overlap for product types: {}", newProductType);

        // Get all operating areas except the one being updated
        Query query = new Query();
        if (excludeAreaId != null) {
            query.addCriteria(Criteria.where("_id").ne(excludeAreaId));
        }

        List<OperatingArea> existingAreas = mongoTemplate.find(query, OperatingArea.class);

        // Parse product types from new area
        Set<String> newProductTypes = parseProductTypes(newProductType);

        for (OperatingArea existingArea : existingAreas) {
            // Parse product types from existing area
            Set<String> existingProductTypes = parseProductTypes(existingArea.getProductType());

            // Check if there's any common product type
            Set<String> commonProductTypes = new HashSet<>(newProductTypes);
            commonProductTypes.retainAll(existingProductTypes);

            if (!commonProductTypes.isEmpty()) {
                log.debug("Found common product types: {} with area: {}", commonProductTypes, existingArea.getName());

                // Check if polygons overlap using MongoDB spatial query
                if (checkPolygonOverlap(newArea, existingArea.getArea())) {
                    String commonTypes = String.join(", ", commonProductTypes);
                    throw new BusinessRuleException(
                            String.format("Area overlaps with existing area '%s' for product type(s): %s. " +
                                            "Two areas with the same product type cannot have overlapping polygons.",
                                    existingArea.getName(), commonTypes)
                    );
                }
            }
        }

        log.debug("No overlapping operating areas found");
    }

    /**
     * Parse product types from semicolon-separated string
     * Example: "HH;KH" -> Set["HH", "KH"]
     * Case-insensitive: converts all to uppercase
     */
    private Set<String> parseProductTypes(String productTypeString) {
        if (productTypeString == null || productTypeString.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(productTypeString.split(";"))
                .map(String::trim)
                .map(String::toUpperCase) // Convert to uppercase for case-insensitive comparison
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Check if two polygons have actual area overlap using JTS
     * Returns true only if polygons have intersecting area (not just touching edges or vertices)
     */
//    private boolean checkPolygonOverlap(
//            org.springframework.data.mongodb.core.geo.GeoJsonPolygon polygon1,
//            org.springframework.data.mongodb.core.geo.GeoJsonPolygon polygon2) {
//
//        try {
//            GeometryFactory geometryFactory = new GeometryFactory();
//
//            // Convert GeoJsonPolygon to JTS Polygon
//            Polygon jtsPolygon1 = convertToJTSPolygon(polygon1, geometryFactory);
//            Polygon jtsPolygon2 = convertToJTSPolygon(polygon2, geometryFactory);
//
//            // Check if polygons intersect
//            if (!jtsPolygon1.intersects(jtsPolygon2)) {
//                log.debug("Polygons do not intersect at all");
//                return false;
//            }
//
//            // Get intersection geometry
//            Geometry intersection = jtsPolygon1.intersection(jtsPolygon2);
//
//            // Check if intersection has area (dimension = 2 means polygon/surface)
//            // dimension = 0 means point, dimension = 1 means line
//            boolean hasAreaOverlap = intersection.getDimension() == 2 && intersection.getArea() > 0;
//
//            if (hasAreaOverlap) {
//                log.debug("Found actual area overlap: intersection area = {}", intersection.getArea());
//            } else {
//                log.debug("No area overlap - polygons only touch at edges or vertices (dimension = {})",
//                        intersection.getDimension());
//            }
//
//            return hasAreaOverlap;
//
//        } catch (Exception e) {
//            log.error("Error checking polygon overlap with JTS", e);
//            return false;
//        }
//    }

    private boolean checkPolygonOverlap(
            org.springframework.data.mongodb.core.geo.GeoJsonPolygon polygon1,
            org.springframework.data.mongodb.core.geo.GeoJsonPolygon polygon2) {

        try {

            GeometryFactory geometryFactory = new GeometryFactory();

            Polygon jtsPolygon1 = convertToJTSPolygon(polygon1, geometryFactory);
            Polygon jtsPolygon2 = convertToJTSPolygon(polygon2, geometryFactory);

            // nếu chỉ chạm cạnh hoặc đỉnh
            if (jtsPolygon1.touches(jtsPolygon2)) {
                log.debug("Polygons only touch at boundary");
                return false;
            }

            Geometry intersection = jtsPolygon1.intersection(jtsPolygon2);

            double overlapArea = intersection.getArea();

            double THRESHOLD = 1e-6; // tùy chỉnh

            log.debug("Overlap area: {}", overlapArea);

            if (overlapArea > THRESHOLD) {
                log.debug("Polygons overlap with significant area");
                return true;
            }

            // check contains
            if (jtsPolygon1.contains(jtsPolygon2) || jtsPolygon2.contains(jtsPolygon1)) {
                log.debug("One polygon contains another");
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("Error checking polygon overlap", e);
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
}

