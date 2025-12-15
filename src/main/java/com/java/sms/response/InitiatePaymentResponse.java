package com.java.sms.response;


import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentResponse {
    private String message;
    private String razorpayOrderId;
    private String receiptNumber;
    private List<Map<String, Object>> fees;
}
