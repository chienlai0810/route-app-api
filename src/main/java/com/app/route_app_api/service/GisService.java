package com.app.route_app_api.service;

import com.app.route_app_api.dto.PointInPolygonResponse;
import com.app.route_app_api.entity.PostOffice;
import com.app.route_app_api.entity.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GIS Service for spatial queries
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GisService {

    private final MongoTemplate mongoTemplate;

    /**
     * Check which routes contain the given point
     */
    public PointInPolygonResponse checkPointInPolygon(double latitude, double longitude) {
        return checkPointInPolygon(latitude, longitude, null);
    }

    /**
     * Check which routes contain the given point with optional product type filter
     */
    public PointInPolygonResponse checkPointInPolygon(double latitude, double longitude, String productType) {
        log.info("Checking point ({}, {}) in polygon with productType: {}", latitude, longitude, productType);

        // Create GeoJSON point for MongoDB query
        GeoJsonPoint point = new GeoJsonPoint(longitude, latitude);

        // Build query criteria
        Criteria criteria = Criteria.where("area").intersects(point);

        // Add product type filter if specified
        if (productType != null && !productType.trim().isEmpty()) {
            // Check if productType contains the specified type (handle semicolon-separated values)
            // Use word boundary or semicolon to match exact product type
            // Pattern: (^|;)HH(;|$) matches HH at start, end, or between semicolons
            String pattern = "(^|;)" + productType.trim() + "(;|$)";
            criteria = criteria.and("productType").regex(pattern, "i");
        }

        Query query = new Query(criteria);

        List<Route> matchingRoutes = mongoTemplate.find(query, Route.class);

        log.info("Found {} matching routes", matchingRoutes.size());

        if (matchingRoutes.isEmpty()) {
            return PointInPolygonResponse.builder()
                    .found(false)
                    .latitude(latitude)
                    .longitude(longitude)
                    .matchingRoutes(new ArrayList<>())
                    .build();
        }

        // Map routes to response
        List<PointInPolygonResponse.RouteInfo> routeInfos = matchingRoutes.stream()
                .map(route -> PointInPolygonResponse.RouteInfo.builder()
                        .id(route.getId())
                        .code(route.getCode())
                        .name(route.getName())
                        .type(route.getType().name())
                        .productType(route.getProductType())
                        .build())
                .collect(Collectors.toList());

        return PointInPolygonResponse.builder()
                .found(true)
                .latitude(latitude)
                .longitude(longitude)
                .matchingRoutes(routeInfos)
                .build();
    }

    /**
     * Find nearest post office to a given point
     */
    public PostOffice findNearestPostOffice(double latitude, double longitude) {
        log.info("Finding nearest post office to ({}, {})", latitude, longitude);

        // MongoDB $near query to find nearest location
        Query query = new Query(
            Criteria.where("location").near(new Point(longitude, latitude))
        ).limit(1);

        List<PostOffice> results = mongoTemplate.find(query, PostOffice.class);

        return results.isEmpty() ? null : results.getFirst();
    }
}

