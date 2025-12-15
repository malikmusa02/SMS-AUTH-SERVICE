package com.java.sms.exception;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    private String message;
    private Object data;

    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(message, data);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(message, null);
    }
}

