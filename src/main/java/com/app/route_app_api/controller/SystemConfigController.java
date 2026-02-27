package com.app.route_app_api.controller;

import com.app.route_app_api.dto.ApiResponse;
import com.app.route_app_api.dto.SystemConfigRequest;
import com.app.route_app_api.dto.SystemConfigResponse;
import com.app.route_app_api.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * Lấy cấu hình hệ thống hiện tại
     * GET /api/system-config
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SystemConfigResponse>> getCurrentConfig() {
        try {
            log.info("GET /api/system-config - Fetching current system configuration");

            SystemConfigResponse config = systemConfigService.getCurrentConfig();

            ApiResponse<SystemConfigResponse> response = ApiResponse.<SystemConfigResponse>builder()
                    .success(true)
                    .message("Lấy cấu hình hệ thống thành công")
                    .data(config)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching system configuration", e);

            ApiResponse<SystemConfigResponse> response = ApiResponse.<SystemConfigResponse>builder()
                    .success(false)
                    .message("Lỗi khi lấy cấu hình hệ thống: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật cấu hình hệ thống
     * PUT /api/system-config
     */
    @PutMapping
    public ResponseEntity<ApiResponse<SystemConfigResponse>> updateConfig(
            @RequestBody SystemConfigRequest request) {
        try {
            log.info("PUT /api/system-config - Updating system configuration");

            SystemConfigResponse config = systemConfigService.updateConfig(request);

            ApiResponse<SystemConfigResponse> response = ApiResponse.<SystemConfigResponse>builder()
                    .success(true)
                    .message("Cập nhật cấu hình hệ thống thành công")
                    .data(config)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating system configuration", e);

            ApiResponse<SystemConfigResponse> response = ApiResponse.<SystemConfigResponse>builder()
                    .success(false)
                    .message("Lỗi khi cập nhật cấu hình hệ thống: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Reset cấu hình về mặc định
     * POST /api/system-config/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> resetConfig() {
        try {
            log.info("POST /api/system-config/reset - Resetting system configuration to default");

            SystemConfigResponse config = systemConfigService.resetToDefault();

            ApiResponse<SystemConfigResponse> response = ApiResponse.<SystemConfigResponse>builder()
                    .success(true)
                    .message("Reset cấu hình hệ thống về mặc định thành công")
                    .data(config)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error resetting system configuration", e);

            ApiResponse<SystemConfigResponse> response = ApiResponse.<SystemConfigResponse>builder()
                    .success(false)
                    .message("Lỗi khi reset cấu hình hệ thống: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

