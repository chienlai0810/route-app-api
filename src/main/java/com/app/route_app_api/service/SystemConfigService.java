package com.app.route_app_api.service;

import com.app.route_app_api.dto.SystemConfigRequest;
import com.app.route_app_api.dto.SystemConfigResponse;
import com.app.route_app_api.entity.SystemConfig;
import com.app.route_app_api.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    /**
     * Lấy cấu hình hệ thống hiện tại
     */
    public SystemConfigResponse getCurrentConfig() {
        SystemConfig config = systemConfigRepository.findFirstByOrderByLastUpdatedDesc()
                .orElseGet(this::createDefaultConfig);

        return convertToResponse(config);
    }

    /**
     * Cập nhật cấu hình hệ thống
     */
    public SystemConfigResponse updateConfig(SystemConfigRequest request) {
        log.info("Updating system configuration");

        // Lấy config hiện tại hoặc tạo mới
        SystemConfig config = systemConfigRepository.findFirstByOrderByLastUpdatedDesc()
                .orElse(new SystemConfig());

        // Cập nhật các giá trị
        if (request.getAntiConflictThreshold() != null) {
            config.setAntiConflictThreshold(request.getAntiConflictThreshold());
        }

        if (request.getAntiConflictUnit() != null) {
            config.setAntiConflictUnit(request.getAntiConflictUnit());
        }

        if (request.getRouteColors() != null && !request.getRouteColors().isEmpty()) {
            config.setRouteColors(request.getRouteColors());
        }

        config.setLastUpdated(LocalDateTime.now());
        config.setUpdatedBy(request.getUpdatedBy() != null ? request.getUpdatedBy() : "system");

        // Lưu vào database
        SystemConfig savedConfig = systemConfigRepository.save(config);

        log.info("System configuration updated successfully with ID: {}", savedConfig.getId());

        return convertToResponse(savedConfig);
    }

    /**
     * Tạo cấu hình mặc định
     */
    private SystemConfig createDefaultConfig() {
        log.info("Creating default system configuration");

        Map<String, String> defaultColors = new HashMap<>();
        defaultColors.put("DELIVERY", "hsl(142, 76%, 36%)"); // Màu xanh lá
        defaultColors.put("PICKUP", "hsl(25, 95%, 53%)"); // Màu cam
        defaultColors.put("ALL", "hsl(187, 85%, 43%)"); // Màu xanh dương

        SystemConfig defaultConfig = SystemConfig.builder()
                .antiConflictThreshold(5.0)
                .antiConflictUnit("%")
                .routeColors(defaultColors)
                .lastUpdated(LocalDateTime.now())
                .updatedBy("system")
                .build();

        return systemConfigRepository.save(defaultConfig);
    }

    /**
     * Chuyển đổi Entity sang Response DTO
     */
    private SystemConfigResponse convertToResponse(SystemConfig config) {
        return SystemConfigResponse.builder()
                .id(config.getId())
                .antiConflictThreshold(config.getAntiConflictThreshold())
                .antiConflictUnit(config.getAntiConflictUnit())
                .routeColors(config.getRouteColors())
                .lastUpdated(config.getLastUpdated())
                .updatedBy(config.getUpdatedBy())
                .build();
    }

    /**
     * Reset về cấu hình mặc định
     */
    public SystemConfigResponse resetToDefault() {
        log.info("Resetting system configuration to default");

        SystemConfig defaultConfig = createDefaultConfig();

        return convertToResponse(defaultConfig);
    }
}

