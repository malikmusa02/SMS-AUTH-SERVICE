package com.java.sms.DataClass;


import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Shape of confirm_payment request expected from front-end.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentRequest {
    private Long studentYearId;
    private List<Map<String, Object>> selectedFees;
    private String paymentMode; // "online" or "cash" etc
    private Long receivedBy;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
