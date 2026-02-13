package com.app.route_app_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GeoJSON Polygon representation for MongoDB
 * Format: { "type": "Polygon", "coordinates": [{lat, lng}, {lat, lng}, ...] }
 * The first and last positions must be equivalent (closed loop)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoJsonPolygon {
    private String type = "Polygon";
    private List<Coordinate> coordinates; // Array of coordinate objects

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinate {
        private Double lat;
        private Double lng;
    }
}

