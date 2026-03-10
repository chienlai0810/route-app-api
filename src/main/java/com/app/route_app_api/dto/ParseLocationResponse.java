package com.app.route_app_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParseLocationResponse {

    private List<Component> components;
    private String addressLine;
    private String formattedAddress;
    private Geometry geometry;
    private String id;
    private Integer version;
    private String code;
    private Double confidence;
    private List<String> originalTokens;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
        private String type;
        private String name;
        private String id;
        private String code;
        private String typeName;
        private Integer level;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        private Location location;
        private Bounds bounds;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private Double lat;
        private Double lng;
        private Double accuracy;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bounds {
        private Location northeast;
        private Location southwest;
    }
}

