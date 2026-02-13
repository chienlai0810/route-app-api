# MongoDB Schema & Index Setup

## Collections Structure

### 1. post_offices Collection

#### Schema
```javascript
{
  "_id": ObjectId,
  "code": String,           // Unique, required
  "name": String,           // Required
  "address": String,        // Required
  "phone": String,          // Optional
  "location": {             // GeoJSON Point, required
    "type": "Point",
    "coordinates": [Double, Double]  // [longitude, latitude]
  },
  "status": String,         // ACTIVE | INACTIVE | MAINTENANCE
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

#### Indexes
```javascript
// Unique index on code
db.post_offices.createIndex({ "code": 1 }, { unique: true })

// 2dsphere index for geospatial queries
db.post_offices.createIndex({ "location": "2dsphere" })
```

#### Sample Data
```javascript
db.post_offices.insertOne({
  "code": "BC-HN-001",
  "name": "Bưu cục Hoàn Kiếm",
  "address": "123 Đường Trần Hưng Đạo, Hoàn Kiếm, Hà Nội",
  "phone": "024-12345678",
  "location": {
    "type": "Point",
    "coordinates": [105.8516, 21.0285]
  },
  "status": "ACTIVE",
  "createdAt": new Date(),
  "updatedAt": new Date()
})
```

---

### 2. routes Collection

#### Schema
```javascript
{
  "_id": ObjectId,
  "code": String,           // Unique, required
  "name": String,           // Required
  "type": String,           // DELIVERY | PICKUP | BOTH
  "postOfficeId": String,   // Reference to post_offices._id
  "staffMain": String,      // Optional
  "staffSub": String,       // Optional
  "area": {                 // GeoJSON Polygon, required
    "type": "Polygon",
    "coordinates": [[[Double, Double], ...]]  // [[[lon, lat], ...]]
  },
  "color": String,          // Hex color code
  "createdAt": ISODate,
  "updatedAt": ISODate
}
```

#### Indexes
```javascript
// Unique index on code
db.routes.createIndex({ "code": 1 }, { unique: true })

// Index on postOfficeId for faster lookups
db.routes.createIndex({ "postOfficeId": 1 })

// 2dsphere index for Point-in-Polygon queries
db.routes.createIndex({ "area": "2dsphere" })
```

#### Sample Data
```javascript
db.routes.insertOne({
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
        [105.8500, 21.0300]  // Must close the polygon
      ]
    ]
  },
  "color": "#FF5733",
  "createdAt": new Date(),
  "updatedAt": new Date()
})
```

---

## GeoJSON Format Notes

### Point Format
```javascript
{
  "type": "Point",
  "coordinates": [longitude, latitude]  // NOTE: longitude first!
}
```

**Example:**
```javascript
// Correct ✓
{ "type": "Point", "coordinates": [105.8516, 21.0285] }

// Wrong ✗
{ "type": "Point", "coordinates": [21.0285, 105.8516] }
```

### Polygon Format
```javascript
{
  "type": "Polygon",
  "coordinates": [
    [  // Outer ring
      [lon1, lat1],
      [lon2, lat2],
      [lon3, lat3],
      [lon4, lat4],
      [lon1, lat1]  // Must close the ring (first point = last point)
    ]
    // Optional: inner rings (holes) can be added as additional arrays
  ]
}
```

**Rules:**
1. Coordinates is a 3D array: `[[[lon, lat], [lon, lat], ...]]`
2. First and last coordinates must be identical (closed loop)
3. Minimum 4 coordinate pairs (3 unique points + 1 closing point)
4. Outer ring must be counter-clockwise
5. Inner rings (holes) must be clockwise

---

## Geospatial Queries

### 1. Find routes containing a point (Point-in-Polygon)
```javascript
db.routes.find({
  area: {
    $geoIntersects: {
      $geometry: {
        type: "Point",
        coordinates: [105.8525, 21.0275]
      }
    }
  }
})
```

### 2. Find post offices near a point
```javascript
db.post_offices.find({
  location: {
    $near: {
      $geometry: {
        type: "Point",
        coordinates: [105.8525, 21.0275]
      },
      $maxDistance: 5000  // 5km in meters
    }
  }
})
```

### 3. Find post offices within a polygon
```javascript
db.post_offices.find({
  location: {
    $geoWithin: {
      $geometry: {
        type: "Polygon",
        coordinates: [
          [
            [105.8500, 21.0300],
            [105.8550, 21.0300],
            [105.8550, 21.0250],
            [105.8500, 21.0250],
            [105.8500, 21.0300]
          ]
        ]
      }
    }
  }
})
```

### 4. Find routes that overlap with a polygon
```javascript
db.routes.find({
  area: {
    $geoIntersects: {
      $geometry: {
        type: "Polygon",
        coordinates: [
          [
            [105.8510, 21.0290],
            [105.8540, 21.0290],
            [105.8540, 21.0260],
            [105.8510, 21.0260],
            [105.8510, 21.0290]
          ]
        ]
      }
    }
  }
})
```

---

## Index Management

### Check existing indexes
```javascript
db.post_offices.getIndexes()
db.routes.getIndexes()
```

### Drop and recreate indexes (if needed)
```javascript
// Drop all indexes except _id
db.post_offices.dropIndexes()
db.routes.dropIndexes()

// Recreate indexes
db.post_offices.createIndex({ "code": 1 }, { unique: true })
db.post_offices.createIndex({ "location": "2dsphere" })

db.routes.createIndex({ "code": 1 }, { unique: true })
db.routes.createIndex({ "postOfficeId": 1 })
db.routes.createIndex({ "area": "2dsphere" })
```

### Index statistics
```javascript
db.post_offices.stats().indexSizes
db.routes.stats().indexSizes
```

---

## Data Validation

### Add validation rules to collections
```javascript
db.createCollection("post_offices", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["code", "name", "address", "location", "status"],
      properties: {
        code: {
          bsonType: "string",
          pattern: "^[A-Z0-9_-]+$",
          description: "must be uppercase alphanumeric with dash/underscore"
        },
        name: {
          bsonType: "string",
          description: "must be a string"
        },
        address: {
          bsonType: "string",
          description: "must be a string"
        },
        phone: {
          bsonType: "string",
          description: "optional phone number"
        },
        location: {
          bsonType: "object",
          required: ["type", "coordinates"],
          properties: {
            type: {
              enum: ["Point"],
              description: "must be Point"
            },
            coordinates: {
              bsonType: "array",
              minItems: 2,
              maxItems: 2,
              description: "must be [longitude, latitude]"
            }
          }
        },
        status: {
          enum: ["ACTIVE", "INACTIVE", "MAINTENANCE"],
          description: "must be ACTIVE, INACTIVE, or MAINTENANCE"
        }
      }
    }
  }
})

db.createCollection("routes", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["code", "name", "type", "postOfficeId", "area"],
      properties: {
        code: {
          bsonType: "string",
          pattern: "^[A-Z0-9_-]+$",
          description: "must be uppercase alphanumeric with dash/underscore"
        },
        name: {
          bsonType: "string",
          description: "must be a string"
        },
        type: {
          enum: ["DELIVERY", "PICKUP", "BOTH"],
          description: "must be DELIVERY, PICKUP, or BOTH"
        },
        postOfficeId: {
          bsonType: "string",
          description: "must be a valid post office ID"
        },
        area: {
          bsonType: "object",
          required: ["type", "coordinates"],
          properties: {
            type: {
              enum: ["Polygon"],
              description: "must be Polygon"
            },
            coordinates: {
              bsonType: "array",
              description: "must be valid polygon coordinates"
            }
          }
        },
        color: {
          bsonType: "string",
          pattern: "^#[0-9A-Fa-f]{6}$",
          description: "must be hex color code"
        }
      }
    }
  }
})
```

---

## Performance Tips

### 1. Index Usage
- Always use indexed fields in queries
- Use `explain()` to check query performance
```javascript
db.routes.find({ postOfficeId: "xxx" }).explain("executionStats")
```

### 2. Compound Indexes
For queries that filter by multiple fields:
```javascript
db.routes.createIndex({ "postOfficeId": 1, "type": 1 })
```

### 3. Projection
Only return needed fields:
```javascript
db.routes.find(
  { postOfficeId: "xxx" },
  { code: 1, name: 1, type: 1 }
)
```

### 4. Geospatial Query Optimization
- Always use 2dsphere indexes for geospatial queries
- Limit result set with `$maxDistance` when possible
- Use bounding box queries before expensive geometric operations

---

## Backup & Restore

### Backup database
```bash
mongodump --db route_app_db --out /backup/path
```

### Restore database
```bash
mongorestore --db route_app_db /backup/path/route_app_db
```

### Export collection to JSON
```bash
mongoexport --db route_app_db --collection post_offices --out post_offices.json --pretty
```

### Import collection from JSON
```bash
mongoimport --db route_app_db --collection post_offices --file post_offices.json
```

---

## Monitoring

### Check database statistics
```javascript
db.stats()
```

### Check collection statistics
```javascript
db.post_offices.stats()
db.routes.stats()
```

### Check slow queries
```javascript
db.setProfilingLevel(2)  // Profile all queries
db.system.profile.find().sort({ ts: -1 }).limit(10)
```

---

## Common Issues & Solutions

### Issue: GeoJSON validation error
**Solution:** Ensure coordinates are [longitude, latitude] and polygon is closed

### Issue: Index not being used
**Solution:** Run `explain()` to check query plan and adjust indexes

### Issue: Slow geospatial queries
**Solution:** Ensure 2dsphere indexes exist and use appropriate query operators

### Issue: Duplicate key error
**Solution:** Code field must be unique across all documents

