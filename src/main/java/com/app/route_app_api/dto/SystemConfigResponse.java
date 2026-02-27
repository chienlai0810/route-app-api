package com.app.route_app_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {

    private String id;

    // Ngưỡng cảnh báo khi tuyến chồng lấn
    private Double antiConflictThreshold;

    // Đơn vị của ngưỡng
    private String antiConflictUnit;

    // Màu sắc cho từng loại tuyến
    private Map<String, String> routeColors;

    // Thời gian cập nhật cuối
    private LocalDateTime lastUpdated;

    // Người cập nhật cuối
    private String updatedBy;
}

