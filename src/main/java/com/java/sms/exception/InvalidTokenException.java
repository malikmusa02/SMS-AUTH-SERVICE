package com.java.sms.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidTokenException extends RuntimeException {
    private final HttpStatus status;

    public InvalidTokenException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
