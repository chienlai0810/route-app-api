package com.app.route_app_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

/**
 * PostOffice Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostOfficeRequest {

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, underscore and dash")
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String phone;

    @NotNull(message = "Location is required")
    private GeoJsonPoint location;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|INACTIVE|MAINTENANCE)$", message = "Status must be ACTIVE, INACTIVE or MAINTENANCE")
    private String status;
}

