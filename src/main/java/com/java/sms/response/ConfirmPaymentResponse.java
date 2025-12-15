package com.java.sms.response;


import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentResponse {
    private String message;
    private List<Map<String, Object>> payments;
}
