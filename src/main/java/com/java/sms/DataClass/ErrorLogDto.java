package com.java.sms.DataClass;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ErrorLogDto {
    private Long id;
    private Long userId;
    private String endpoint;
    private String method;
    private Integer statusCode;
    private String errorType;
    private String errorMessage;
    private String tracebackInfo;
    private String timestamp;
}
