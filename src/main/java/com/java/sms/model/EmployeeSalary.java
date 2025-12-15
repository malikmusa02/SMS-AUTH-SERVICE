package com.java.sms.model;

import com.java.sms.model.enums.MonthEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Represents monthly salary details of an employee.
 * Mirrors Django EmployeeSalary model.
 */
@Entity
@Table(
        name = "employee_salaries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_month_school_year",
                        columnNames = {"employee_id", "month", "school_year_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSalary {

    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee reference.
     * Django: ForeignKey(Employee, CASCADE)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Gross salary amount (base salary).
     */
    @Column(name = "gross_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal grossAmount;

    /**
     * Salary deductions.
     */
    @Column(name = "deductions", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO;

    /**
     * Bonus amount.
     */
    @Column(name = "bonus", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    /**
     * Net salary after deductions/bonus.
     */
    @Column(name = "net_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal netAmount;

    /**
     * Salary month.
     * Enum used instead of String (safer than Django CharField).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "month", length = 20, nullable = false)
    private MonthEnum month;

    /**
     * SchoolYear ID from Django service.
     * Stored as Long instead of FK.
     */
    @Column(name = "school_year_id", nullable = false)
    private Long schoolYearId;

    /**
     * User who issued the salary.
     * Django: paid_by = ForeignKey(User, SET_NULL)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_user_id")
    private User paidBy;

    /**
     * Optional remarks.
     */
    @Column(name = "remarks", length = 225)
    private String remarks;

    /**
     * Creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Optional payment reference.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    /**
     * Auto set createdAt on insert.
     */
    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * String representation similar to Django __str__.
     */
    @Override
    public String toString() {
        return employee.getUser().getFirstName() + " " +
                month + " " + netAmount;
    }
}
