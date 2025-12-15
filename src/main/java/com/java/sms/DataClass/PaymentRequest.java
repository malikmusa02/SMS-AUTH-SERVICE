package com.java.sms.DataClass;



import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true, message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "paymentMethod is required")
    private String paymentMethod; // CASH, CHEQUE, ONLINE

    @NotNull(message = "status is required")
    private String status; // PENDING, SUCCESS, FAILED

    @Size(max = 225)
    private String remarks;

    @NotNull(message = "paymentDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentDate;

    @Size(max = 50)
    private String chequeNumber;

    // RazorpayX fields (optional)
    @Size(max = 100)
    private String payoutId;

    @Size(max = 100)
    private String fundAccountId;

    @Size(max = 100)
    private String contactId;
}
