# Route App API - Hệ thống quản lý Bưu cục & Tuyến giao/nhận hàng với GIS

## Tổng quan
Backend API được xây dựng bằng **Java Spring Boot + MongoDB** để quản lý bưu cục và tuyến giao/nhận hàng có tích hợp GIS (Geographic Information System).

## Công nghệ sử dụng
- **Java 21**
- **Spring Boot 4.0.2**
- **MongoDB** (với 2dsphere index)
- **Spring Data MongoDB**
- **Jakarta Validation**
- **Lombok**

## Cấu trúc dự án
```
route-app-api/
├── src/main/java/com/app/route_app_api/
│   ├── config/              # Cấu hình MongoDB, Indexes
│   ├── controller/          # REST Controllers
│   ├── dto/                 # Data Transfer Objects
│   ├── entity/              # MongoDB Entities
│   ├── exception/           # Exception handling
│   ├── model/               # GeoJSON models
│   ├── repository/          # MongoDB Repositories
│   └── service/             # Business logic
└── src/main/resources/
    └── application.properties
```

## Cài đặt và chạy

### Yêu cầu
- Java 21+
- MongoDB 4.0+ (cài đặt local hoặc Docker)
- Maven 3.6+

### Bước 1: Khởi động MongoDB
```bash
# Sử dụng Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Hoặc khởi động MongoDB local
mongod --dbpath /path/to/data
```

### Bước 2: Cấu hình kết nối
Mở file `src/main/resources/application.properties` và cập nhật:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/route_app_db
```

### Bước 3: Build và chạy
```bash
# Build project
./mvnw clean install

# Chạy application
./mvnw spring-boot:run
```

API sẽ chạy tại: `http://localhost:8080`

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Response Format
Tất cả API response đều có format chuẩn:
```json
{
  "success": true,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2024-01-01T10:00:00"
}
```

Error response:
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message",
  "path": "/api/v1/...",
  "validationErrors": {
    "fieldName": "Error message"
  }
}
```

---

## 1. PostOffice API (Bưu cục)

### 1.1 Tạo Bưu cục
**POST** `/api/v1/post-offices`

Request Body:
```json
{
  "code": "BC-HN-001",
  "name": "Bưu cục Hoàn Kiếm",
  "address": "123 Đường Trần Hưng Đạo, Hoàn Kiếm, Hà Nội",
  "phone": "024-12345678",
  "location": {
    "type": "Point",
    "coordinates": [105.8516, 21.0285]
  },
  "status": "ACTIVE"
}
```

Response (201 Created):
```json
{
  "success": true,
  "message": "Post office created successfully",
  "data": {
    "id": "65a1b2c3d4e5f6789abcdef0",
    "code": "BC-HN-001",
    "name": "Bưu cục Hoàn Kiếm",
    "address": "123 Đường Trần Hưng Đạo, Hoàn Kiếm, Hà Nội",
    "phone": "024-12345678",
    "location": {
      "type": "Point",
      "coordinates": [105.8516, 21.0285]
    },
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### 1.2 Lấy tất cả Bưu cục
**GET** `/api/v1/post-offices`

Response (200 OK):
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "65a1b2c3d4e5f6789abcdef0",
      "code": "BC-HN-001",
      "name": "Bưu cục Hoàn Kiếm",
      "address": "123 Đường Trần Hưng Đạo, Hoàn Kiếm, Hà Nội",
      "phone": "024-12345678",
      "location": {
        "type": "Point",
        "coordinates": [105.8516, 21.0285]
      },
      "status": "ACTIVE",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "timestamp": "2024-01-15T10:35:00"
}
```

### 1.3 Lấy Bưu cục theo ID
**GET** `/api/v1/post-offices/{id}`

### 1.4 Lấy Bưu cục theo Code
**GET** `/api/v1/post-offices/code/{code}`

### 1.5 Cập nhật Bưu cục
**PUT** `/api/v1/post-offices/{id}`

Request Body: Same as create

### 1.6 Xóa Bưu cục
**DELETE** `/api/v1/post-offices/{id}`

---

## 2. Route API (Tuyến)

### 2.1 Tạo Tuyến
**POST** `/api/v1/routes`

Request Body:
```json
{
  "code": "TUYEN-HK-01",
  "name": "Tuyến Hoàn Kiếm 1",
  "type": "DELIVERY",
  "postOfficeId": "65a1b2c3d4e5f6789abcdef0",
  "staffMain": "Nguyễn Văn A",
  "staffSub": "Trần Văn B",
  "area": {
    "type": "Polygon",
    "coordinates": [
      [
        [105.8500, 21.0300],
        [105.8550, 21.0300],
        [105.8550, 21.0250],
        [105.8500, 21.0250],
        [105.8500, 21.0300]
      ]
    ]
  },
  "color": "#FF5733"
}
```

**Lưu ý về GeoJSON Polygon:**
- Coordinates là array 3 chiều: `[[[lon, lat], [lon, lat], ...]]`
- Điểm đầu và điểm cuối phải giống nhau (closed loop)
- Thứ tự: [longitude, latitude] (không phải latitude, longitude!)

Response (201 Created):
```json
{
  "success": true,
  "message": "Route created successfully",
  "data": {
    "id": "65a1b2c3d4e5f6789abcdef1",
    "code": "TUYEN-HK-01",
    "name": "Tuyến Hoàn Kiếm 1",
    "type": "DELIVERY",
    "postOfficeId": "65a1b2c3d4e5f6789abcdef0",
    "postOfficeName": "Bưu cục Hoàn Kiếm",
    "staffMain": "Nguyễn Văn A",
    "staffSub": "Trần Văn B",
    "area": {
      "type": "Polygon",
      "coordinates": [
        [
          [105.8500, 21.0300],
          [105.8550, 21.0300],
          [105.8550, 21.0250],
          [105.8500, 21.0250],
          [105.8500, 21.0300]
        ]
      ]
    },
    "color": "#FF5733",
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00"
  },
  "timestamp": "2024-01-15T11:00:00"
}
```

### 2.2 Lấy tất cả Tuyến
**GET** `/api/v1/routes`

Query Parameters:
- `postOfficeId` (optional): Lọc theo bưu cục
- `type` (optional): Lọc theo loại (DELIVERY, PICKUP, BOTH)

Examples:
- `/api/v1/routes` - Tất cả tuyến
- `/api/v1/routes?postOfficeId=65a1b2c3d4e5f6789abcdef0` - Tuyến của bưu cục cụ thể
- `/api/v1/routes?type=DELIVERY` - Chỉ tuyến giao hàng

### 2.3 Lấy Tuyến theo ID
**GET** `/api/v1/routes/{id}`

### 2.4 Lấy Tuyến theo Code
**GET** `/api/v1/routes/code/{code}`

### 2.5 Cập nhật Tuyến
**PUT** `/api/v1/routes/{id}`

Request Body: Same as create

### 2.6 Xóa Tuyến
**DELETE** `/api/v1/routes/{id}`

---

## 3. GIS API (Point-in-Polygon Check)

### 3.1 Kiểm tra tọa độ thuộc tuyến nào (POST)
**POST** `/api/v1/gis/check-point`

Request Body:
```json
{
  "latitude": 21.0275,
  "longitude": 105.8525
}
```

Response (200 OK):
```json
{
  "success": true,
  "message": "Point is within 1 route(s)",
  "data": {
    "found": true,
    "latitude": 21.0275,
    "longitude": 105.8525,
    "matchingRoutes": [
      {
        "id": "65a1b2c3d4e5f6789abcdef1",
        "code": "TUYEN-HK-01",
        "name": "Tuyến Hoàn Kiếm 1",
        "type": "DELIVERY",
        "color": "#FF5733"
      }
    ],
    "postOffice": {
      "id": "65a1b2c3d4e5f6789abcdef0",
      "code": "BC-HN-001",
      "name": "Bưu cục Hoàn Kiếm",
      "address": "123 Đường Trần Hưng Đạo, Hoàn Kiếm, Hà Nội"
    }
  },
  "timestamp": "2024-01-15T11:30:00"
}
```

### 3.2 Kiểm tra tọa độ thuộc tuyến nào (GET)
**GET** `/api/v1/gis/check-point?latitude=21.0275&longitude=105.8525`

Response: Same as POST

---

## Business Rules

### 1. Validation Rules
- **Code**: Phải là chữ IN HOA, số, dấu gạch ngang hoặc gạch dưới
- **Status**: Chỉ nhận ACTIVE, INACTIVE, hoặc MAINTENANCE
- **Type**: DELIVERY (giao), PICKUP (nhận), BOTH (cả hai)
- **GeoJSON**: Phải đúng format chuẩn GeoJSON

### 2. Overlap Detection
- Tuyến mới không được chồng lấn quá **5%** với tuyến hiện có
- Khi update tuyến, hệ thống sẽ kiểm tra overlap với các tuyến khác
- Nếu vi phạm, API trả về lỗi 400 với message chi tiết

### 3. Relationships
- 1 Bưu cục có nhiều Tuyến
- Mỗi Tuyến thuộc về 1 Bưu cục
- Không thể xóa Bưu cục nếu đang có Tuyến (implement cascading delete nếu cần)

---

## MongoDB Schema & Indexes

### PostOffice Collection
```javascript
{
  "_id": ObjectId("..."),
  "code": "BC-HN-001",
  "name": "Bưu cục Hoàn Kiếm",
  "address": "123 Đường Trần Hưng Đạo, Hoàn Kiếm, Hà Nội",
  "phone": "024-12345678",
  "location": {
    "type": "Point",
    "coordinates": [105.8516, 21.0285]
  },
  "status": "ACTIVE",
  "createdAt": ISODate("2024-01-15T10:30:00Z"),
  "updatedAt": ISODate("2024-01-15T10:30:00Z")
}
```

**Indexes:**
- `code`: Unique index
- `location`: 2dsphere index (for geospatial queries)

### Route Collection
```javascript
{
  "_id": ObjectId("..."),
  "code": "TUYEN-HK-01",
  "name": "Tuyến Hoàn Kiếm 1",
  "type": "DELIVERY",
  "postOfficeId": "65a1b2c3d4e5f6789abcdef0",
  "staffMain": "Nguyễn Văn A",
  "staffSub": "Trần Văn B",
  "area": {
    "type": "Polygon",
    "coordinates": [
      [
        [105.8500, 21.0300],
        [105.8550, 21.0300],
        [105.8550, 21.0250],
        [105.8500, 21.0250],
        [105.8500, 21.0300]
      ]
    ]
  },
  "color": "#FF5733",
  "createdAt": ISODate("2024-01-15T11:00:00Z"),
  "updatedAt": ISODate("2024-01-15T11:00:00Z")
}
```

**Indexes:**
- `code`: Unique index
- `postOfficeId`: Regular index
- `area`: 2dsphere index (for Point-in-Polygon queries)

---

## Testing với Postman/cURL

### Tạo Bưu cục
```bash
curl -X POST http://localhost:8080/api/v1/post-offices \
  -H "Content-Type: application/json" \
  -d '{
    "code": "BC-HN-001",
    "name": "Bưu cục Hoàn Kiếm",
    "address": "123 Đường Trần Hưng Đạo, Hoàn Kiếm, Hà Nội",
    "phone": "024-12345678",
    "location": {
      "type": "Point",
      "coordinates": [105.8516, 21.0285]
    },
    "status": "ACTIVE"
  }'
```

### Tạo Tuyến
```bash
curl -X POST http://localhost:8080/api/v1/routes \
  -H "Content-Type: application/json" \
  -d '{
    "code": "TUYEN-HK-01",
    "name": "Tuyến Hoàn Kiếm 1",
    "type": "DELIVERY",
    "postOfficeId": "YOUR_POST_OFFICE_ID",
    "staffMain": "Nguyễn Văn A",
    "staffSub": "Trần Văn B",
    "area": {
      "type": "Polygon",
      "coordinates": [
        [
          [105.8500, 21.0300],
          [105.8550, 21.0300],
          [105.8550, 21.0250],
          [105.8500, 21.0250],
          [105.8500, 21.0300]
        ]
      ]
    },
    "color": "#FF5733"
  }'
```

### Kiểm tra Point-in-Polygon
```bash
curl -X GET "http://localhost:8080/api/v1/gis/check-point?latitude=21.0275&longitude=105.8525"
```

---

## Production Considerations

### 1. Security
- Thêm Spring Security cho authentication/authorization
- Implement JWT tokens
- Rate limiting cho API

### 2. Performance
- MongoDB indexes đã được tạo tự động
- Cân nhắc thêm caching (Redis)
- Pagination cho list APIs

### 3. Overlap Detection
- Hiện tại là placeholder implementation
- Production nên dùng **JTS (Java Topology Suite)** để tính toán polygon intersection chính xác
- Thêm dependency:
```xml
<dependency>
    <groupId>org.locationtech.jts</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.19.0</version>
</dependency>
```

### 4. Logging & Monitoring
- Đã tích hợp SLF4J logging
- Thêm monitoring tools (Prometheus, Grafana)
- Health check endpoints

### 5. Testing
- Thêm Unit tests cho Services
- Integration tests cho Controllers
- GIS query testing với MongoDB testcontainers

---

## Troubleshooting

### MongoDB Connection Error
```
Error: Unable to connect to MongoDB
```
**Solution:** Đảm bảo MongoDB đang chạy và connection string đúng trong `application.properties`

### GeoJSON Validation Error
```
Error: Invalid GeoJSON format
```
**Solution:** 
- Đảm bảo coordinates theo format [longitude, latitude]
- Polygon phải closed (điểm đầu = điểm cuối)
- Coordinates là array 3 chiều cho Polygon

### Overlap Error
```
Error: Route overlaps X% with existing route
```
**Solution:** Điều chỉnh polygon để giảm overlap hoặc tăng threshold trong code

---

## License
MIT License

## Contributors
- Development Team

## Support
For issues and questions, please create an issue in the repository.

