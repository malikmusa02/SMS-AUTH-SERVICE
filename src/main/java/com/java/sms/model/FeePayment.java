package com.java.sms.model;


import com.java.sms.model.enums.PaymentMethod;
import com.java.sms.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owning StudentFee
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_fee_id", nullable = false)
    private StudentFee studentFee;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50, nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /**
     * Reference to Django authentication.User id (received_by)
     */
    @Column(name = "received_by_id")
    private Long receivedById;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "cheque_number", length = 50, unique = true)
    private String chequeNumber;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;



    @Override
    public String toString() {
        return "Payment #" + id + " - ₹" + amount + " - " + paymentMethod + " - studentFeeId:" + (studentFee != null ? studentFee.getId() : null);
    }
}
