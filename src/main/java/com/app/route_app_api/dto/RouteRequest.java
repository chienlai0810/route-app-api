package com.app.route_app_api.dto;

import com.app.route_app_api.entity.Route;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;

/**
 * Route Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotBlank(message = "Code is required")
//    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, underscore and dash")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Post office ID is required")
    private String postOfficeId;

    @NotNull(message = "Route type is required")
    private Route.RouteType type;

    @NotBlank(message = "Product type is required")
    private String productType; // Có thể chứa nhiều giá trị cách nhau bởi dấu ;, ví dụ: HH;TH

    private String staffMain;

    private String staffSub;

    private String color; // Màu sắc để highlight tuyến (hex color code, ví dụ: #FF5733)

    @NotNull(message = "Area is required")
    private GeoJsonPolygon area;
}



