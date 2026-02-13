package com.app.route_app_api.repository;

import com.app.route_app_api.entity.PostOffice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PostOffice Repository
 */
@Repository
public interface PostOfficeRepository extends MongoRepository<PostOffice, String> {

    Optional<PostOffice> findByCode(String code);

    boolean existsByCode(String code);

    // Search by code containing (case-insensitive)
    List<PostOffice> findByCodeContainingIgnoreCase(String code);

    // Search by name containing (case-insensitive)
    List<PostOffice> findByNameContainingIgnoreCase(String name);

    // Search by code or name containing (case-insensitive)
    @Query("{'$or': [{'code': {$regex: ?0, $options: 'i'}}, {'name': {$regex: ?0, $options: 'i'}}, {'address': {$regex: ?0, $options: 'i'}}]}")
    List<PostOffice> searchByCodeOrName(String keyword);
}

