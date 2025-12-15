package com.java.sms.DataClass;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDiscountRequest {
    @NotBlank
    private String discountName;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal discountedAmountPercent;
}
