package com.app.route_app_api.controller;

import com.app.route_app_api.dto.ApiResponse;
import com.app.route_app_api.dto.PostOfficeRequest;
import com.app.route_app_api.dto.PostOfficeResponse;
import com.app.route_app_api.service.PostOfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PostOffice REST Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/post-offices")
@RequiredArgsConstructor
public class PostOfficeController {

    private final PostOfficeService postOfficeService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostOfficeResponse>> createPostOffice(
            @Valid @RequestBody PostOfficeRequest request) {
        log.info("POST /api/v1/post-offices - Creating post office: {}", request);

        PostOfficeResponse response = postOfficeService.createPostOffice(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post office created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostOfficeResponse>> getPostOfficeById(@PathVariable String id) {
        log.info("GET /api/v1/post-offices/{} - Getting post office by id", id);

        PostOfficeResponse response = postOfficeService.getPostOfficeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PostOfficeResponse>> getPostOfficeByCode(@PathVariable String code) {
        log.info("GET /api/v1/post-offices/code/{} - Getting post office by code", code);

        PostOfficeResponse response = postOfficeService.getPostOfficeByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostOfficeResponse>>> getAllPostOffices(
            @RequestParam(required = false) String search) {
        log.info("GET /api/v1/post-offices - search: {}", search);

        List<PostOfficeResponse> response = postOfficeService.searchPostOffices(search);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostOfficeResponse>> updatePostOffice(
            @PathVariable String id,
            @Valid @RequestBody PostOfficeRequest request) {
        log.info("PUT /api/v1/post-offices/{} - Updating post office", id);

        PostOfficeResponse response = postOfficeService.updatePostOffice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Post office updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePostOffice(@PathVariable String id) {
        log.info("DELETE /api/v1/post-offices/{} - Deleting post office", id);

        postOfficeService.deletePostOffice(id);
        return ResponseEntity.ok(ApiResponse.success("Post office deleted successfully", null));
    }
}

