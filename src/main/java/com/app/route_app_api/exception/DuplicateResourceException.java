package com.app.route_app_api.exception;

/**
 * Exception for duplicate resource
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

