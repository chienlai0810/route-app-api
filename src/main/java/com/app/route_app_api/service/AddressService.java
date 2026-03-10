package com.app.route_app_api.service;

import com.app.route_app_api.dto.AddressCheckResponse;
import com.app.route_app_api.dto.ParseLocationResponse;
import com.app.route_app_api.dto.PointInPolygonResponse;
import com.app.route_app_api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for address geocoding using ViettelPost Location API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final RestTemplate restTemplate;
    private final GisService gisService;

    @Value("${viettelpost.location.api.url:https://dev-io.viettelpost.vn/location/v2.0/addresses}")
    private String viettelPostApiUrl;

    /**
     * Check address and get latitude/longitude
     * Then check if the point belongs to any route
     */
    public AddressCheckResponse checkAddress(String address, String productType) {
        log.info("Checking address: {} with productType: {}", address, productType);

        // Call ViettelPost Location API
        ParseLocationResponse locationData = callViettelPostApi(address);

        if (locationData == null || locationData.getGeometry() == null
                || locationData.getGeometry().getLocation() == null) {
            throw new ResourceNotFoundException("Could not find location for address: " + address);
        }

        // Extract location data
        Double latitude = locationData.getGeometry().getLocation().getLat();
        Double longitude = locationData.getGeometry().getLocation().getLng();
        Double accuracy = locationData.getGeometry().getLocation().getAccuracy();

        log.info("Found location: lat={}, lng={}, accuracy={}", latitude, longitude, accuracy);

        // Check if point is in any route
        PointInPolygonResponse routeInfo = gisService.checkPointInPolygon(
                latitude, longitude, productType);

        // Convert components
        List<AddressCheckResponse.AddressComponent> components = null;
        if (locationData.getComponents() != null) {
            components = locationData.getComponents().stream()
                    .map(c -> AddressCheckResponse.AddressComponent.builder()
                            .type(c.getType())
                            .name(c.getName())
                            .typeName(c.getTypeName())
                            .level(c.getLevel())
                            .id(c.getId())
                            .code(c.getCode())
                            .build())
                    .collect(Collectors.toList());
        }

        // Build response
        return AddressCheckResponse.builder()
                .originalAddress(address)
                .formattedAddress(locationData.getFormattedAddress())
                .latitude(latitude)
                .longitude(longitude)
                .accuracy(accuracy)
                .components(components)
                .routeInfo(routeInfo)
                .build();
    }

    /**
     * Call ViettelPost Location API to geocode address
     */
    private ParseLocationResponse callViettelPostApi(String address) {
        try {
            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Cookie", "SERVERID=2");

            // Build request body (array with single address)
            List<String> addresses = Collections.singletonList(address);
            HttpEntity<List<String>> requestEntity = new HttpEntity<>(addresses, headers);

            // Build URL with query parameters
            String url = viettelPostApiUrl + "?shortForm=true&system=VTP";

            log.info("Calling ViettelPost API: {} with address: {}", url, address);

            // Make API call
            ResponseEntity<List<ParseLocationResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<ParseLocationResponse>>() {}
            );

            // Extract first result
            if (response.getBody() != null && !response.getBody().isEmpty()) {
                ParseLocationResponse result = response.getBody().get(0);
                log.info("ViettelPost API response: formattedAddress={}, confidence={}",
                        result.getFormattedAddress(), result.getConfidence());
                return result;
            }

            log.warn("ViettelPost API returned empty response");
            return null;

        } catch (Exception e) {
            log.error("Error calling ViettelPost API for address: {}", address, e);
            throw new RuntimeException("Failed to geocode address: " + e.getMessage(), e);
        }
    }
}

