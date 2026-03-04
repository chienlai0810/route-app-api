package com.app.route_app_api.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for GeoJsonHelper utility
 * Demonstrates how to work with MongoDB GeoJSON format
 */
class GeoJsonHelperTest {

    @Test
    void testCreatePolygon() {
        // Tạo một polygon hình chữ nhật ở Hà Nội
        // Note: MongoDB GeoJSON sử dụng [longitude, latitude] order
        List<List<Double>> coordinates = List.of(
            List.of(105.8342, 21.0278),  // [lng, lat] - điểm 1
            List.of(105.8442, 21.0278),  // [lng, lat] - điểm 2
            List.of(105.8442, 21.0378),  // [lng, lat] - điểm 3
            List.of(105.8342, 21.0378),  // [lng, lat] - điểm 4
            List.of(105.8342, 21.0278)   // [lng, lat] - phải đóng vòng (giống điểm 1)
        );

        GeoJsonPolygon polygon = GeoJsonHelper.createPolygon(coordinates);

        assertNotNull(polygon);
        assertEquals(5, polygon.getPoints().size());
        assertTrue(GeoJsonHelper.isValidPolygon(polygon));
    }

    @Test
    void testCreatePoint() {
        double longitude = 105.8392;
        double latitude = 21.0328;

        GeoJsonPoint point = GeoJsonHelper.createPoint(longitude, latitude);

        assertNotNull(point);
        assertEquals(longitude, point.getX());
        assertEquals(latitude, point.getY());
    }

    @Test
    void testExtractCoordinates() {
        List<Point> points = List.of(
            new Point(105.8342, 21.0278),
            new Point(105.8442, 21.0278),
            new Point(105.8442, 21.0378),
            new Point(105.8342, 21.0378),
            new Point(105.8342, 21.0278)
        );

        GeoJsonPolygon polygon = new GeoJsonPolygon(points);
        List<List<Double>> extracted = GeoJsonHelper.extractCoordinates(polygon);

        assertEquals(5, extracted.size());
        assertEquals(105.8342, extracted.get(0).get(0)); // longitude của điểm đầu
        assertEquals(21.0278, extracted.get(0).get(1));  // latitude của điểm đầu
    }

    @Test
    void testInvalidPolygon_NotClosed() {
        // Polygon không đóng vòng (điểm đầu khác điểm cuối)
        List<List<Double>> coordinates = List.of(
            List.of(105.8342, 21.0278),
            List.of(105.8442, 21.0278),
            List.of(105.8442, 21.0378),
            List.of(105.8342, 21.0378)  // Thiếu điểm cuối
        );

        assertThrows(IllegalArgumentException.class, () -> {
            GeoJsonHelper.createPolygon(coordinates);
        });
    }

    @Test
    void testInvalidPolygon_TooFewPoints() {
        // Polygon phải có ít nhất 4 điểm
        List<List<Double>> coordinates = List.of(
            List.of(105.8342, 21.0278),
            List.of(105.8442, 21.0278),
            List.of(105.8342, 21.0278)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            GeoJsonHelper.createPolygon(coordinates);
        });
    }
}

