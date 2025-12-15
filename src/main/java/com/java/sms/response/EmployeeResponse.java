package com.java.sms.response;


import lombok.Builder;
import lombok.Data;



import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for Employee.
 * Equivalent to Django EmployeeSerializer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    /** Employee ID */
    private Long id;

    /** Linked user ID */
    private Long user;

    /** Full name (user.get_full_name) */
    private String name;

    /** User roles (teacher / office staff etc.) */
    private List<String> role;

    /** Base salary */
    private BigDecimal baseSalary;
}


