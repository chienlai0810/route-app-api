package com.app.route_app_api.service;

import com.app.route_app_api.dto.OperatingAreaRequest;
import com.app.route_app_api.dto.OperatingAreaResponse;
import com.app.route_app_api.entity.OperatingArea;
import com.app.route_app_api.entity.PostOffice;
import com.app.route_app_api.exception.ResourceNotFoundException;
import com.app.route_app_api.repository.OperatingAreaRepository;
import com.app.route_app_api.repository.PostOfficeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Operating Area Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperatingAreaService {

    private final OperatingAreaRepository operatingAreaRepository;
    private final PostOfficeRepository postOfficeRepository;

    @Transactional
    public OperatingAreaResponse createOperatingArea(OperatingAreaRequest request) {
        log.info("Creating operating area: {}", request.getName());

        // Validate post office exists
        PostOffice postOffice = postOfficeRepository.findById(request.getPostOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Post office not found with id: " + request.getPostOfficeId()));

        OperatingArea operatingArea = OperatingArea.builder()
                .name(request.getName())
                .postOfficeId(request.getPostOfficeId())
                .productType(request.getProductType())
                .area(request.getArea())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OperatingArea saved = operatingAreaRepository.save(operatingArea);
        log.info("Created operating area with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public OperatingAreaResponse getOperatingAreaById(String id) {
        log.info("Getting operating area by id: {}", id);

        OperatingArea operatingArea = operatingAreaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + id));

        return mapToResponse(operatingArea);
    }

    public List<OperatingAreaResponse> getAllOperatingAreas() {
        log.info("Getting all operating areas");

        return operatingAreaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OperatingAreaResponse> getOperatingAreasByPostOfficeId(String postOfficeId) {
        log.info("Getting operating areas by post office id: {}", postOfficeId);

        // Validate post office exists
        if (!postOfficeRepository.existsById(postOfficeId)) {
            throw new ResourceNotFoundException("Post office not found with id: " + postOfficeId);
        }

        return operatingAreaRepository.findByPostOfficeId(postOfficeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<OperatingAreaResponse> getOperatingAreasByProductType(String productType) {
        log.info("Getting operating areas by product type: {}", productType);

        return operatingAreaRepository.findByProductTypeContaining(productType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OperatingAreaResponse updateOperatingArea(String id, OperatingAreaRequest request) {
        log.info("Updating operating area with id: {}", id);

        OperatingArea operatingArea = operatingAreaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operating area not found with id: " + id));

        // Validate post office exists
        PostOffice postOffice = postOfficeRepository.findById(request.getPostOfficeId())
                .orElseThrow(() -> new ResourceNotFoundException("Post office not found with id: " + request.getPostOfficeId()));

        operatingArea.setName(request.getName());
        operatingArea.setPostOfficeId(request.getPostOfficeId());
        operatingArea.setProductType(request.getProductType());
        operatingArea.setArea(request.getArea());
        operatingArea.setUpdatedAt(LocalDateTime.now());

        OperatingArea updated = operatingAreaRepository.save(operatingArea);
        log.info("Updated operating area with id: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteOperatingArea(String id) {
        log.info("Deleting operating area with id: {}", id);

        if (!operatingAreaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Operating area not found with id: " + id);
        }

        operatingAreaRepository.deleteById(id);
        log.info("Deleted operating area with id: {}", id);
    }

    private OperatingAreaResponse mapToResponse(OperatingArea operatingArea) {
        String postOfficeName = null;
        if (operatingArea.getPostOfficeId() != null) {
            postOfficeName = postOfficeRepository.findById(operatingArea.getPostOfficeId())
                    .map(PostOffice::getName)
                    .orElse(null);
        }

        return OperatingAreaResponse.builder()
                .id(operatingArea.getId())
                .name(operatingArea.getName())
                .postOfficeId(operatingArea.getPostOfficeId())
                .postOfficeName(postOfficeName)
                .productType(operatingArea.getProductType())
                .area(operatingArea.getArea())
                .createdAt(operatingArea.getCreatedAt())
                .updatedAt(operatingArea.getUpdatedAt())
                .build();
    }
}

