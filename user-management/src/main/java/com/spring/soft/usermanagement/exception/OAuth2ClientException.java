package com.spring.soft.usermanagement.exception;

public class OAuth2ClientException extends RuntimeException {
    public OAuth2ClientException(String message) {
        super(message);
    }

    public OAuth2ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
