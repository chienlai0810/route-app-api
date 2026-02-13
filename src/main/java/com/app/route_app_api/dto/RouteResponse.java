package com.app.route_app_api.dto;

import com.app.route_app_api.entity.Route;
import com.app.route_app_api.model.GeoJsonPolygon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Route Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private String id;
    private String code;
    private String name;
    private Route.RouteType type;
    private String postOfficeId;
    private String postOfficeName; // Thêm tên bưu cục để tiện hiển thị
    private String staffMain;
    private String staffSub;
    private GeoJsonPolygon area;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

