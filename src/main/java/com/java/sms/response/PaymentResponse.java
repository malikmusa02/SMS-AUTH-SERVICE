package com.java.sms.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentDate;

    private String chequeNumber;
    private String payoutId;
    private String fundAccountId;
    private String contactId;
    private LocalDateTime createdAt;
}
