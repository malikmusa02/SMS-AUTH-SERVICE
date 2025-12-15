package com.java.sms.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents an employee with a base salary.
 * Mirrors Django Employee model.
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    /**
     * Primary key for Employee table.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one mapping with authentication User.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Base salary of the employee.
     * Decimal(10,2)
     */
    @Column(name = "base_salary", precision = 10, scale = 2, nullable = false)
    private BigDecimal baseSalary;

    /**
     * String representation
     */
    @Override
    public String toString() {
        return user.getFirstName()+ " " + user.getMiddleName() + " - base_salary " + baseSalary;
    }
}
