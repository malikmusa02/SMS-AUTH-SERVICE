package com.java.sms.response;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePaymentResponse {

    private Long id;

    private Long studentFeeId;

    private BigDecimal amount;

    private String paymentMethod;

    private String status;

    private String notes;

    private LocalDateTime paymentDate;

    private String chequeNumber;

    private String razorpayPaymentId;

    private String razorpayOrderId;

    private String razorpaySignature;

    private LocalDateTime createdAt;
}
