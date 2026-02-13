package com.app.route_app_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GeoJSON Point representation for MongoDB
 * Format: { "type": "Point", "coordinates": {lat, lng} }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoJsonPoint {
    private String type = "Point";
    private Coordinate coordinates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinate {
        private Double lat;
        private Double lng;
    }

    public GeoJsonPoint(double lat, double lng) {
        this.type = "Point";
        this.coordinates = new Coordinate(lat, lng);
    }

    public double getLongitude() {
        return coordinates != null ? coordinates.getLng() : 0.0;
    }

    public double getLatitude() {
        return coordinates != null ? coordinates.getLat() : 0.0;
    }
}

