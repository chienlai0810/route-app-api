package com.app.route_app_api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Operating Area Entity - Vùng hoạt động của bưu cục
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "operating_areas")
public class OperatingArea {

    @Id
    private String id;

    private String name; // Tên vùng hoạt động

    private String postOfficeId; // ID của bưu cục quản lý vùng này

    private String productType; // Loại hàng hóa (có thể nhiều giá trị cách nhau bởi dấu ;, ví dụ: HH;TH)

    private GeoJsonPolygon area; // Khu vực phục vụ (Polygon) - MongoDB GeoJSON format

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

