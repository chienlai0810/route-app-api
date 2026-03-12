package com.app.route_app_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Operating Area Status Check
 * Used to determine if operating area can be deleted/updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingAreaStatusResponse {

    private String operatingAreaId;

    private String operatingAreaName;

    private boolean hasRoutes;

    private long routeCount;

    private boolean canDelete;

    private boolean canUpdate;

    private String message;
}

