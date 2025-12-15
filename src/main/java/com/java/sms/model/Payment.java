package com.java.sms.model;



import com.java.sms.model.enums.PaymentMethod;
import com.java.sms.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment",
        indexes = {
                @Index(name = "idx_payment_cheque_number", columnList = "cheque_number")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20, nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "remarks", length = 225)
    private String remarks;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "cheque_number", length = 50, unique = true)
    private String chequeNumber;

    // RazorpayX / payout related fields
    @Column(name = "payout_id", length = 100)
    private String payoutId;

    @Column(name = "fund_account_id", length = 100)
    private String fundAccountId;

    @Column(name = "contact_id", length = 100)
    private String contactId;



    @Override
    public String toString() {
        return paymentMethod + " - ₹" + amount + " (" + status + ")";
    }
}
