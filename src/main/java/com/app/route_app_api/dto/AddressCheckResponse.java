package com.app.route_app_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Address Check Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressCheckResponse {

    private String originalAddress;
    private String formattedAddress;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private List<AddressComponent> components;

    // Route information if found
    private PointInPolygonResponse routeInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressComponent {
        private String type;
        private String name;
        private String typeName;
        private Integer level;
        private String id;
        private String code;
    }
}
