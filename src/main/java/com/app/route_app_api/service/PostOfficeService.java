package com.app.route_app_api.service;

import com.app.route_app_api.dto.PostOfficeRequest;
import com.app.route_app_api.dto.PostOfficeResponse;
import com.app.route_app_api.entity.PostOffice;
import com.app.route_app_api.exception.DuplicateResourceException;
import com.app.route_app_api.exception.ResourceNotFoundException;
import com.app.route_app_api.repository.PostOfficeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PostOffice Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostOfficeService {

    private final PostOfficeRepository postOfficeRepository;

    @Transactional
    public PostOfficeResponse createPostOffice(PostOfficeRequest request) {
        log.info("Creating post office with code: {}", request.getCode());

        // Check for duplicate code
        if (postOfficeRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Post office with code " + request.getCode() + " already exists");
        }

        PostOffice postOffice = PostOffice.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .location(request.getLocation())
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PostOffice saved = postOfficeRepository.save(postOffice);
        log.info("Created post office with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public PostOfficeResponse getPostOfficeById(String id) {
        log.info("Getting post office by id: {}", id);

        PostOffice postOffice = postOfficeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post office not found with id: " + id));

        return mapToResponse(postOffice);
    }

    public PostOfficeResponse getPostOfficeByCode(String code) {
        log.info("Getting post office by code: {}", code);

        PostOffice postOffice = postOfficeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Post office not found with code: " + code));

        return mapToResponse(postOffice);
    }

    public List<PostOfficeResponse> searchPostOffices(String search) {
        log.info("Searching post offices with search term: {}", search);

        List<PostOffice> postOffices;

        if (search != null && !search.trim().isEmpty()) {
            // Search by term in both code and name
            postOffices = postOfficeRepository.searchByCodeOrName(search.trim());
            log.info("Found {} post offices matching search term: {}", postOffices.size(), search);
        } else {
            // No search criteria, return all
            postOffices = postOfficeRepository.findAll();
            log.info("No search criteria, returning all {} post offices", postOffices.size());
        }

        return postOffices.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostOfficeResponse updatePostOffice(String id, PostOfficeRequest request) {
        log.info("Updating post office with id: {}", id);

        PostOffice postOffice = postOfficeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post office not found with id: " + id));

        // Check for duplicate code if code is being changed
        if (!postOffice.getCode().equals(request.getCode()) &&
            postOfficeRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Post office with code " + request.getCode() + " already exists");
        }

        postOffice.setCode(request.getCode());
        postOffice.setName(request.getName());
        postOffice.setAddress(request.getAddress());
        postOffice.setPhone(request.getPhone());
        postOffice.setLocation(request.getLocation());
        postOffice.setStatus(request.getStatus());
        postOffice.setUpdatedAt(LocalDateTime.now());

        PostOffice updated = postOfficeRepository.save(postOffice);
        log.info("Updated post office with id: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Transactional
    public void deletePostOffice(String id) {
        log.info("Deleting post office with id: {}", id);

        if (!postOfficeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post office not found with id: " + id);
        }

        postOfficeRepository.deleteById(id);
        log.info("Deleted post office with id: {}", id);
    }

    private PostOfficeResponse mapToResponse(PostOffice postOffice) {
        return PostOfficeResponse.builder()
                .id(postOffice.getId())
                .code(postOffice.getCode())
                .name(postOffice.getName())
                .address(postOffice.getAddress())
                .phone(postOffice.getPhone())
                .location(postOffice.getLocation())
                .status(postOffice.getStatus())
                .createdAt(postOffice.getCreatedAt())
                .updatedAt(postOffice.getUpdatedAt())
                .build();
    }
}

