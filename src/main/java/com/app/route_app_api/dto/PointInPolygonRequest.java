package com.app.route_app_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Point-in-Polygon Check Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointInPolygonRequest {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}

