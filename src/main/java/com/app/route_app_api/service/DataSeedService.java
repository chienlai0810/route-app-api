package com.app.route_app_api.service;

import com.app.route_app_api.entity.PostOffice;
import com.app.route_app_api.entity.Route;
import com.app.route_app_api.repository.PostOfficeRepository;
import com.app.route_app_api.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.geo.GeoJsonPolygon;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for seeding sample data into database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeedService {

    private final PostOfficeRepository postOfficeRepository;
    private final RouteRepository routeRepository;
    private final Random random = new Random();

    private static final String[] COLORS = {
            "#FF5733", "#33FF57", "#3357FF", "#FF33F5", "#F5FF33",
            "#33FFF5", "#FF8C33", "#8C33FF", "#33FF8C", "#FF3333",
            "#33FF33", "#3333FF", "#FFFF33", "#FF33FF", "#33FFFF",
            "#FFA500", "#800080", "#008080", "#FF1493", "#00CED1"
    };

    private static final String[] PRODUCT_TYPES = {"HH", "KH", "TH", "HH;KH", "HH;TH", "KH;TH"};
    private static final Route.RouteType[] ROUTE_TYPES = {Route.RouteType.DELIVERY, Route.RouteType.PICKUP, Route.RouteType.ALL};
    private static final String[] STAFF_NAMES = {
            "Nguyễn Văn A", "Trần Thị B", "Lê Văn C", "Phạm Thị D", "Hoàng Văn E",
            "Đặng Thị F", "Vũ Văn G", "Bùi Thị H", "Đỗ Văn I", "Ngô Thị K"
    };

    /**
     * Seed data vào database
     * @param force - true để xóa dữ liệu cũ và tạo mới
     */
    public String seedData(boolean force) {
        if (force) {
            log.info("Clearing existing data...");
            routeRepository.deleteAll();
            postOfficeRepository.deleteAll();
        }

        // Check if post offices exist, if not seed them
        List<PostOffice> postOffices = postOfficeRepository.findAll();
        if (postOffices.isEmpty()) {
            log.info("No post offices found. Seeding post offices...");
            postOffices = seedPostOffices();
        }

        // Check if routes already exist
        if (!force && routeRepository.count() > 0) {
            return "Database already contains routes. Use force=true to override.";
        }


        // Seed Routes for each Post Office
        int totalRoutes = 0;
        for (PostOffice postOffice : postOffices) {
            List<Route> routes = seedRoutesForPostOffice(postOffice);
            totalRoutes += routes.size();
            log.info("Created {} routes for post office: {}", routes.size(), postOffice.getName());
        }

        return String.format("Successfully seeded %d routes for %d post offices",
                totalRoutes, postOffices.size());
    }

    /**
     * Xóa toàn bộ dữ liệu
     */
    public void clearAllData() {
        log.info("Clearing all data...");
        routeRepository.deleteAll();
        postOfficeRepository.deleteAll();
        log.info("All data cleared");
    }

    /**
     * Xóa chỉ routes
     */
    public void clearRoutes() {
        log.info("Clearing all routes...");
        routeRepository.deleteAll();
        log.info("All routes cleared");
    }

    /**
     * Seed Post Offices ở các vị trí cách xa nhau để tránh chồng lấn tuyến
     * Mỗi bưu cục cách nhau ít nhất 0.1 độ (~10km) để đảm bảo vùng phủ 200 tuyến không chồng lấn
     */
    private List<PostOffice> seedPostOffices() {
        List<PostOffice> postOffices = new ArrayList<>();

        // Tọa độ trung tâm Hà Nội
        double baseLatitude = 21.028511;
        double baseLongitude = 105.804817;

        // Khoảng cách giữa các bưu cục: 0.1 độ (~10km)
        // Điều này đảm bảo vùng phủ của mỗi bưu cục (6km x 2km) không chồng lấn
        double spacing = 0.1;

        // Tạo 50 bưu cục trong lưới 7x8 (để phân bố đều)
        int cols = 8; // Số cột
        int rows = 7; // Số hàng (7x8 = 56, chỉ lấy 50)

        for (int i = 1; i <= 50; i++) {
            int row = (i - 1) / cols;
            int col = (i - 1) % cols;

            // Tính offset từ trung tâm để phân bố đều
            double lngOffset = (col - cols / 2.0) * spacing;
            double latOffset = (row - rows / 2.0) * spacing;

            String code = String.format("BC-%02d", i);
            String name = String.valueOf(i);

            GeoJsonPoint location = new GeoJsonPoint(
                baseLongitude + lngOffset,
                baseLatitude + latOffset
            );

            PostOffice postOffice = PostOffice.builder()
                    .code(code)
                    .name(name)
                    .address("Địa chỉ bưu cục " + i)
                    .location(location)
                    .phone("024-" + (38000000 + random.nextInt(10000)))
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            postOffices.add(postOfficeRepository.save(postOffice));
            log.info("Created post office: {} at ({}, {})",
                name, location.getX(), location.getY());
        }

        return postOffices;
    }

    private List<Route> seedRoutesForPostOffice(PostOffice postOffice) {
        List<Route> routes = new ArrayList<>();
        GeoJsonPoint center = postOffice.getLocation();
        double baseLng = center.getX();
        double baseLat = center.getY();

        // Kích thước mỗi ô polygon (giảm xuống để tránh chồng lấn giữa các bưu cục)
        // Với 10x20 = 200 ô, tổng vùng phủ: 0.06 lng x 0.02 lat (~6km x 2km)
        double cellWidth = 0.001;  // Chiều rộng mỗi ô (longitude) - ~300m
        double cellHeight = 0.001; // Chiều cao mỗi ô (latitude) - ~200m

        // Tạo lưới 10x20 = 200 routes xung quanh bưu cục (lấp kín liền nhau với nhiều hình dạng)
        int routeIndex = 0;
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 20; col++) {
                routeIndex++;

                // Tính toán vị trí góc dưới trái của mỗi ô
                // Bắt đầu từ góc (-10, -5) để tạo lưới đối xứng quanh bưu cục
                double startLng = baseLng + (col - 10) * cellWidth;
                double startLat = baseLat + (row - 5) * cellHeight;

                // Tạo polygon với nhiều hình dạng khác nhau nhưng vẫn lấp kín
                GeoJsonPolygon polygon = createTilingPolygon(startLng, startLat, cellWidth, cellHeight, routeIndex, row, col);

                Route route = Route.builder()
                        .code(postOffice.getCode() + "-R" + String.format("%03d", routeIndex))
                        .name("Tuyến " + postOffice.getName().replace("Bưu cục ", "") + " - " + routeIndex)
                        .postOfficeId(postOffice.getId())
                        .type(ROUTE_TYPES[random.nextInt(ROUTE_TYPES.length)])
                        .productType(PRODUCT_TYPES[random.nextInt(PRODUCT_TYPES.length)])
                        .staffMain(STAFF_NAMES[random.nextInt(STAFF_NAMES.length)])
                        .staffSub(routeIndex % 2 == 0 ? STAFF_NAMES[random.nextInt(STAFF_NAMES.length)] : null)
                        .area(polygon)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                routes.add(routeRepository.save(route));
            }
        }

        return routes;
    }

    /**
     * Tạo polygon lấp kín với nhiều hình dạng khác nhau
     * Các hình dạng được thiết kế để lấp kín liền nhau không có khoảng trống
     */
    private GeoJsonPolygon createTilingPolygon(double startLng, double startLat, double width, double height,
                                                int routeIndex, int row, int col) {
        List<org.springframework.data.geo.Point> points = new ArrayList<>();

        // Chọn hình dạng dựa trên vị trí trong lưới để đảm bảo lấp kín
        int shapeType = (row + col) % 5; // 5 loại hình dạng

        switch (shapeType) {
            case 0 -> {
                // Hình chữ nhật thông thường
                points.add(new org.springframework.data.geo.Point(startLng, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width, startLat + height));
                points.add(new org.springframework.data.geo.Point(startLng, startLat + height));
            }
            case 1 -> {
                // Hình thang (mặt trên hẹp hơn)
                double indent = width * 0.15; // Thu hẹp 15% mỗi bên
                points.add(new org.springframework.data.geo.Point(startLng, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width - indent, startLat + height));
                points.add(new org.springframework.data.geo.Point(startLng + indent, startLat + height));
            }
            case 2 -> {
                // Hình thang ngược (mặt dưới hẹp hơn)
                double indent = width * 0.15;
                points.add(new org.springframework.data.geo.Point(startLng + indent, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width - indent, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width, startLat + height));
                points.add(new org.springframework.data.geo.Point(startLng, startLat + height));
            }
            case 3 -> {
                // Hình ngũ giác (thêm 1 điểm ở giữa mặt trên)
                double midLng = startLng + width / 2;
                points.add(new org.springframework.data.geo.Point(startLng, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width, startLat + height * 0.7));
                points.add(new org.springframework.data.geo.Point(midLng, startLat + height)); // Đỉnh giữa
                points.add(new org.springframework.data.geo.Point(startLng, startLat + height * 0.7));
            }
            case 4 -> {
                // Hình lục giác nằm ngang (thêm 2 điểm ở giữa 2 bên)
                double midLat = startLat + height / 2;
                points.add(new org.springframework.data.geo.Point(startLng + width * 0.15, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width * 0.85, startLat));
                points.add(new org.springframework.data.geo.Point(startLng + width, midLat));
                points.add(new org.springframework.data.geo.Point(startLng + width * 0.85, startLat + height));
                points.add(new org.springframework.data.geo.Point(startLng + width * 0.15, startLat + height));
                points.add(new org.springframework.data.geo.Point(startLng, midLat));
            }
        }

        // Đóng polygon (điểm cuối = điểm đầu)
        points.add(points.getFirst());

        return new GeoJsonPolygon(points);
    }

}

