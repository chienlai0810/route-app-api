package com.app.route_app_api.exception;

/**
 * Exception for business rule violations
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

