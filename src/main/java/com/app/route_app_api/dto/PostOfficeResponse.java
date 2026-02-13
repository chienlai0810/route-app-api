package com.app.route_app_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;

/**
 * PostOffice Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostOfficeResponse {

    private String id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private GeoJsonPoint location;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

