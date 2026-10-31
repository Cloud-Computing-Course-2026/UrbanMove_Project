package com.urbanmove.rides;

import org.springframework.http.HttpStatus;

public class DomainException extends RuntimeException {
    final HttpStatus status;

    public DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    static DomainException conflict(String message) {
        return new DomainException(HttpStatus.CONFLICT, message);
    }

    static DomainException notFound(String message) {
        return new DomainException(HttpStatus.NOT_FOUND, message);
    }
}
