package com.urbanmove.rides.exception;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {
    private final HttpStatus status;

    public HttpStatus getStatus() {
        return status;
    }

    public DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static DomainException conflict(String message) {
        return new DomainException(HttpStatus.CONFLICT, message);
    }

    public static DomainException notFound(String message) {
        return new DomainException(HttpStatus.NOT_FOUND, message);
    }
}
