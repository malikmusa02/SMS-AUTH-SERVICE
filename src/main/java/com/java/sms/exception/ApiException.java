package com.java.sms.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Simple runtime exception used to carry an HTTP status.
 */
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
    }

}
