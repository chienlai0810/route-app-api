package com.app.route_app_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Point-in-Polygon Check Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointInPolygonResponse {

    private boolean found;
    private Double latitude;
    private Double longitude;
    private List<RouteInfo> matchingRoutes;
    private PostOfficeInfo postOffice;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteInfo {
        private String id;
        private String code;
        private String name;
        private String type;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostOfficeInfo {
        private String id;
        private String code;
        private String name;
        private String address;
    }
}

