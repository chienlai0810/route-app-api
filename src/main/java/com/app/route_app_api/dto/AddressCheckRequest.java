package com.app.route_app_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address Check Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCheckRequest {

    @NotBlank(message = "Address is required")
    private String address;

    private String productType; // Loại hàng hóa (HH, KH, TH) - optional filter
}

