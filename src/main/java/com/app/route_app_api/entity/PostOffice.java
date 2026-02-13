package com.app.route_app_api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * PostOffice Entity - Bưu cục
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "post_offices")
public class PostOffice {

    @Id
    private String id;

    private String code; // Mã bưu cục (indexed programmatically)

    private String name; // Tên bưu cục

    private String address; // Địa chỉ

    private String phone; // Số điện thoại

    private GeoJsonPoint location; // Vị trí GeoJSON Point (indexed programmatically)

    private String status; // ACTIVE, INACTIVE, MAINTENANCE

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

