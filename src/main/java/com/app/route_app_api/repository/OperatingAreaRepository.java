package com.app.route_app_api.repository;

import com.app.route_app_api.entity.OperatingArea;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Operating Area Repository
 */
@Repository
public interface OperatingAreaRepository extends MongoRepository<OperatingArea, String> {

    List<OperatingArea> findByPostOfficeId(String postOfficeId);

    List<OperatingArea> findByProductTypeContaining(String productType);
}

