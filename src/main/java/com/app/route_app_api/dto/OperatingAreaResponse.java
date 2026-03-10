package com.app.route_app_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

import java.time.LocalDateTime;

/**
 * Operating Area Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingAreaResponse {

    private String id;
    private String name;
    private String postOfficeId;
    private String postOfficeName; // Thêm tên bưu cục để tiện hiển thị
    private String productType; // Có thể chứa nhiều giá trị cách nhau bởi dấu ;, ví dụ: HH;TH
    private GeoJsonPolygon area;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

