package com.app.route_app_api.exception;

/**
 * Exception for resource not found
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

