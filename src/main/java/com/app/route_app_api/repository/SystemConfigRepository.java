package com.app.route_app_api.repository;

import com.app.route_app_api.entity.SystemConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends MongoRepository<SystemConfig, String> {

    // Tìm cấu hình mới nhất
    Optional<SystemConfig> findFirstByOrderByLastUpdatedDesc();
}

