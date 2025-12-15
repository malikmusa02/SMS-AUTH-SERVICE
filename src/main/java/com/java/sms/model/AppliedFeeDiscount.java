package com.java.sms.model;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "applied_fee_discount")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppliedFeeDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to Django StudentYearLevel PK (stored as id only).
     * Django owns the student table; we store the FK id.
     */
    @Column(name = "student_year_id", nullable = false)
    private Long studentYearId;

    /**
     * Local FeeStructure entity (this table is in Spring DB).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(name = "discount_name", nullable = false, length = 100)
    private String discountName;

    @Column(name = "discount_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal discountAmount;


    // Approved by: reference to your User entity (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    @CreationTimestamp
    private OffsetDateTime approvedAt;

    @Override
    public String toString() {
        return discountName + " - " + discountAmount + " - studentYearId:" + studentYearId;
    }
}
