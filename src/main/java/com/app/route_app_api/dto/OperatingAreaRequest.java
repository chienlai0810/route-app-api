package com.app.route_app_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

/**
 * Operating Area Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingAreaRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Post office ID is required")
    private String postOfficeId;

    @NotBlank(message = "Product type is required")
    private String productType; // Có thể chứa nhiều giá trị cách nhau bởi dấu ;, ví dụ: HH;TH

    @NotNull(message = "Area is required")
    private GeoJsonPolygon area;
}

