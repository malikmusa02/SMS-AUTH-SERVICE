package com.java.sms.DataClass;


import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePaymentRequest {

    @NotNull(message = "studentFeeId is required")
    private Long studentFeeId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod; // CASH, ONLINE, CHEQUE

    @NotBlank(message = "status is required")
    private String status; // PENDING, SUCCESS, FAILED, REFUNDED

    private String notes;

    private LocalDateTime paymentDate;

    private String chequeNumber;

    private String razorpayPaymentId;

    private String razorpayOrderId;

    private String razorpaySignature;
}

