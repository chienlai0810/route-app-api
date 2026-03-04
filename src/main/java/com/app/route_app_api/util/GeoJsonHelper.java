package com.app.route_app_api.util;

import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with GeoJSON objects
 */
public class GeoJsonHelper {

    /**
     * Create a GeoJsonPolygon from a list of coordinates
     * @param coordinates List of [longitude, latitude] pairs
     * @return GeoJsonPolygon object
     *
     * Example:
     * <pre>
     * List&lt;List&lt;Double&gt;&gt; coords = List.of(
     *     List.of(105.8342, 21.0278),  // lng, lat
     *     List.of(105.8442, 21.0278),
     *     List.of(105.8442, 21.0378),
     *     List.of(105.8342, 21.0378),
     *     List.of(105.8342, 21.0278)   // Phải đóng vòng
     * );
     * GeoJsonPolygon polygon = GeoJsonHelper.createPolygon(coords);
     * </pre>
     */
    public static GeoJsonPolygon createPolygon(List<List<Double>> coordinates) {
        if (coordinates == null || coordinates.size() < 4) {
            throw new IllegalArgumentException("Polygon must have at least 4 points (first and last must be the same)");
        }

        List<Point> points = new ArrayList<>();
        for (List<Double> coord : coordinates) {
            if (coord.size() != 2) {
                throw new IllegalArgumentException("Each coordinate must have exactly 2 elements [longitude, latitude]");
            }
            points.add(new Point(coord.get(0), coord.get(1))); // longitude, latitude
        }

        // Verify first and last points are the same (closed loop)
        Point first = points.get(0);
        Point last = points.get(points.size() - 1);
        if (first.getX() != last.getX() || first.getY() != last.getY()) {
            throw new IllegalArgumentException("Polygon must be closed (first and last points must be the same)");
        }

        return new GeoJsonPolygon(points);
    }

    /**
     * Create a GeoJsonPoint from latitude and longitude
     * @param longitude Longitude (kinh độ)
     * @param latitude Latitude (vĩ độ)
     * @return GeoJsonPoint object
     */
    public static GeoJsonPoint createPoint(double longitude, double latitude) {
        return new GeoJsonPoint(longitude, latitude);
    }

    /**
     * Extract coordinates from GeoJsonPolygon as List of [lng, lat] pairs
     * @param polygon GeoJsonPolygon object
     * @return List of coordinate pairs
     */
    public static List<List<Double>> extractCoordinates(GeoJsonPolygon polygon) {
        if (polygon == null) {
            return new ArrayList<>();
        }

        List<List<Double>> coordinates = new ArrayList<>();
        for (Point point : polygon.getPoints()) {
            coordinates.add(List.of(point.getX(), point.getY())); // longitude, latitude
        }
        return coordinates;
    }

    /**
     * Validate if a polygon is properly formed
     * @param polygon GeoJsonPolygon to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPolygon(GeoJsonPolygon polygon) {
        if (polygon == null) {
            return false;
        }

        List<Point> points = polygon.getPoints();
        if (points.size() < 4) {
            return false;
        }

        // Check if first and last points are the same
        Point first = points.get(0);
        Point last = points.get(points.size() - 1);
        return first.getX() == last.getX() && first.getY() == last.getY();
    }
}

