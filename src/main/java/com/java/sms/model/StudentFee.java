package com.java.sms.model;


import com.java.sms.model.enums.FeeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_fee",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_student_year_fee_month_schoolyear",
                columnNames = {"student_year_id", "fee_structure_id", "month", "school_year_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to Django StudentYearLevel PK
     */
    @Column(name = "student_year_id", nullable = false)
    private Long studentYearId;

    /**
     * Local FeeStructure (this entity exists in Spring)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    /**
     * 1..12 month number. null allowed for non-monthly items.
     */
    @Column(name = "month")
    private Integer month;

    /**
     * Reference to Django SchoolYear PK (stored as id).
     */
    @Column(name = "school_year_id", nullable = false)
    private Long schoolYearId;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "original_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal originalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "due_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal dueAmount = BigDecimal.ZERO;

    @Column(name = "penalty_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "applied_discount", nullable = false)
    private Boolean appliedDiscount = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FeeStatus status = FeeStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Unique receipt number. generated if missing before persist.
     */
    @Column(name = "receipt_number", length = 50, unique = true, nullable = false, updatable = false)
    private String receiptNumber;

    @PrePersist
    public void ensureReceiptNumber() {
        if (this.receiptNumber == null || this.receiptNumber.isBlank()) {
            this.receiptNumber = generateReceiptNumber();
        }
    }

    private String generateReceiptNumber() {
        // simple random alphanumeric, 10 chars
        final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALPHANUM.charAt(rnd.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "StudentYearId:" + studentYearId + " - " + feeStructure.getFeeType() + " - paid " + paidAmount + " - due " + dueAmount + " - " + status;
    }


}
