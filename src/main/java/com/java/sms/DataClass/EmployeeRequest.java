package com.java.sms.DataClass;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating Employee.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequest {

    /** User ID (read-only in Django serializer, but required in create) */
    @NotNull(message = "User is required")
    private Long user;

    /** Base salary validation same as Django */
    @NotNull
    @Min(value = 1000, message = "Base salary must be at least 1,000")
    @Max(value = 100000, message = "Base salary must not exceed 100,000")
    private BigDecimal baseSalary;
}
