package com.java.sms.response;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class AppliedFeeDiscountResponse {
    private Long id;
    private Long StudentYearLevel;             // student_year id (Django)
    private String studentName;         // computed from Feign response
    private Long feeTypeId;             // fee structure id
    private String feeTypeName;         // fee type name (e.g. "Admission Fee")
    private String discountName;
    private BigDecimal discountAmount;
    private Double discountedAmountPercent;
    private String approvedBy;          // username
    private OffsetDateTime approvedAt;
}



