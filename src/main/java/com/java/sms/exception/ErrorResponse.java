package com.java.sms.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int statusCode;
    private String error;
    private String path;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String path) {
        this.timestamp = timestamp;
        this.statusCode = status;
        this.error = error;
        this.path = path;
    }


}
