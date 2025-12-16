package com.java.sms.DataClass;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating/updating a StudentFee (single fee).
 * Mirrors fields you accepted in Django serializer create.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFeeRequest {
    
    @NotNull
    private Long studentYearId;

    @NotNull
    private Long feeStructureId;

    // 1..12 or null
    private Integer month;

    @NotNull
    private Long schoolYearId;

    private LocalDate dueDate;

    // amount the client intends to pay now (optional)
    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal amountPaid;

    private String paymentMethod; // "online" or "cash" / "cheque"
    private String chequeNumber;
}

