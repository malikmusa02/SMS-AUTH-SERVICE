package com.java.sms.DataClass;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructureRequest {

    @NotNull
    @JsonProperty("masterFeeId")
    private Long masterFeeId;

    @NotNull
    @Size(max = 100)
    @JsonProperty("feeType")
    private String feeType; // e.g., "TUITION_FEE" or "Tuition Fee" - mapper will normalize

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @JsonProperty("feeAmount")
    private BigDecimal feeAmount;

    /**
     * List of YearLevel IDs that exist in Django. Must be validated.
     */
    @JsonProperty("yearLevelIds")
    private Set<Long> yearLevelIds;
}

