package com.app.route_app_api.entity;

import com.app.route_app_api.model.GeoJsonPolygon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Route Entity - Tuyến giao/nhận hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "routes")
public class Route {

    @Id
    private String id;

    private String code; // Mã tuyến (indexed programmatically)

    private String name; // Tên tuyến

    private RouteType type; // DELIVERY, PICKUP, ALL

    private String productType; // Loại hàng hóa (có thể nhiều giá trị cách nhau bởi dấu ;, ví dụ: HH;TH)

    private String staffMain; // Nhân viên chính

    private String staffSub; // Nhân viên phụ

    private GeoJsonPolygon area; // Khu vực phục vụ (Polygon) (indexed programmatically)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum RouteType {
        DELIVERY,   // Chỉ giao hàng
        PICKUP,     // Chỉ nhận hàng
        ALL        // Cả giao và nhận
    }

    public enum ProductType {
        HH,
        KH,
        TH
    }
}

