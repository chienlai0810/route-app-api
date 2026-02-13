package com.app.route_app_api.repository;

import com.app.route_app_api.entity.Route;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Route Repository
 */
@Repository
public interface RouteRepository extends MongoRepository<Route, String> {

    Optional<Route> findByCode(String code);

    boolean existsByCode(String code);

    List<Route> findByType(Route.RouteType type);
}

