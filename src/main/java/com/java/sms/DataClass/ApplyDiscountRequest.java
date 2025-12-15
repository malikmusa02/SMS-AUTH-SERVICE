package com.java.sms.DataClass;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyDiscountRequest {

    @NotNull
    @JsonProperty("studentYearId")
    private Long studentYearId;     // Django StudentYearLevel id

    @NotNull
    @JsonProperty("feeStructureId")
    private Long feeStructureId;    // local FeeStructure id

    @NotBlank
    @JsonProperty("discountName")
    private String discountName;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal discountedAmountPercent;
}


