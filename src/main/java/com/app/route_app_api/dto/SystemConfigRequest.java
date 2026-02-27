package com.app.route_app_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigRequest {

    // Ngưỡng cảnh báo khi tuyến chồng lấn
    private Double antiConflictThreshold;

    // Đơn vị của ngưỡng (%, m, km)
    private String antiConflictUnit;

    // Màu sắc cho từng loại tuyến: GIAO_HANG, NHAN_HANG, TAT_CA, XEM_TRUOC_GIAO_HANG, XEM_TRUOC_NHAN_HANG, XEM_TRUOC_TAT_CA
    private Map<String, String> routeColors;

    // Người cập nhật
    private String updatedBy;
}

