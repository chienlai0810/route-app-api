# Route App API - Post Office & Route Management System with GIS

🚀 **Production-ready Backend API** built with Java Spring Boot + MongoDB for managing post offices and delivery/pickup routes with full GIS (Geographic Information System) support.

## 📋 Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Business Rules](#business-rules)
- [Testing](#testing)
- [Deployment](#deployment)

## ✨ Features

### Core Features
- ✅ **CRUD Operations** for Post Offices (Bưu cục)
- ✅ **CRUD Operations** for Routes (Tuyến giao/nhận hàng)
- ✅ **GIS Integration** with MongoDB 2dsphere indexes
- ✅ **Point-in-Polygon Detection** - determine which route/post office a coordinate belongs to
- ✅ **Route Overlap Validation** - prevent routes from overlapping beyond threshold (5%)
- ✅ **Route Classification** - DELIVERY, PICKUP, or BOTH

### Technical Features
- ✅ RESTful API with standard response format
- ✅ Input validation with Jakarta Validation
- ✅ Global exception handling
- ✅ Automatic MongoDB index creation
- ✅ Clean architecture with separation of concerns
- ✅ Lombok for reduced boilerplate
- ✅ Comprehensive logging with SLF4J
- ✅ GeoJSON format support (Point & Polygon)

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming Language |
| Spring Boot | 4.0.2 | Application Framework |
| MongoDB | 4.0+ | NoSQL Database |
| Spring Data MongoDB | 4.0.2 | Data Access Layer |
| Jakarta Validation | 3.0 | Input Validation |
| Lombok | Latest | Code Generation |
| Maven | 3.6+ | Build Tool |

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- MongoDB 4.0 or higher
- Maven 3.6+

### 1. Start MongoDB
```bash
# Using Docker (recommended)
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Or use local MongoDB installation
mongod --dbpath /path/to/data
```

### 2. Configure Application
Edit `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/route_app_db
```

### 3. Build and Run
```bash
# Build project
./mvnw clean install

# Run application (Windows PowerShell)
./mvnw spring-boot:run
```

### 4. Access API
- API Base URL: `http://localhost:8080/api/v1`

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    REST Controllers                      │
│  PostOfficeController │ RouteController │ GisController │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│                   Service Layer                          │
│  PostOfficeService │ RouteService │ GisService          │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│                Repository Layer                          │
│  PostOfficeRepository │ RouteRepository                 │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│              MongoDB with 2dsphere Indexes               │
│  post_offices collection │ routes collection            │
└─────────────────────────────────────────────────────────┘
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints Overview

#### Post Office Management
- `POST /post-offices` - Create new post office
- `GET /post-offices` - Get all post offices
- `GET /post-offices/{id}` - Get post office by ID
- `GET /post-offices/code/{code}` - Get post office by code
- `PUT /post-offices/{id}` - Update post office
- `DELETE /post-offices/{id}` - Delete post office

#### Route Management
- `POST /routes` - Create new route
- `GET /routes` - Get all routes (supports filters)
- `GET /routes/{id}` - Get route by ID
- `GET /routes/code/{code}` - Get route by code
- `GET /routes?postOfficeId={id}` - Get routes by post office
- `GET /routes?type={type}` - Get routes by type
- `PUT /routes/{id}` - Update route
- `DELETE /routes/{id}` - Delete route

#### GIS Operations
- `POST /gis/check-point` - Check which route(s) contain a point
- `GET /gis/check-point?latitude={lat}&longitude={lon}` - Same as POST

**📄 Full API Documentation:** See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

**📦 Postman Collection:** Import [postman_collection.json](postman_collection.json)

## 🗄️ Database Schema

**📄 Full Schema Documentation:** See [MONGODB_SCHEMA.md](MONGODB_SCHEMA.md)

## 📐 Business Rules

### Validation Rules
1. **Code Format:** Uppercase alphanumeric with dash/underscore only
2. **Status Values:** ACTIVE, INACTIVE, MAINTENANCE
3. **Route Types:** DELIVERY (giao), PICKUP (nhận), BOTH (cả hai)
4. **GeoJSON:** Must follow standard GeoJSON format

### GIS Rules
1. **Coordinates Order:** Always [longitude, latitude]
2. **Polygon Closure:** First and last coordinates must be identical
3. **Minimum Points:** Polygon requires at least 4 coordinate pairs

### Overlap Detection
- Routes cannot overlap more than **5%** with existing routes
- Validation runs on CREATE and UPDATE operations

## 🧪 Testing

### Postman Testing
1. Import `postman_collection.json`
2. Set variables: `postOfficeId`, `routeId`
3. Run collection

## 📦 Deployment

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for Docker and production deployment instructions.

## 📝 License
MIT License

---

**Built with ❤️ using Java Spring Boot + MongoDB**

