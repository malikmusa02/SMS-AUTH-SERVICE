package com.java.sms.DataClass;



import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterFeeRequest {

    /**
     * Accepts "monthly", "quarterly", "yearly", "others" (case-insensitive).
     */

    @JsonProperty("paymentStructure")
    @NotBlank(message = "paymentStructure is required")
    private String paymentStructure;
}

