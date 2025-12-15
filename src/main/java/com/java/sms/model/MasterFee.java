package com.java.sms.model;


import com.java.sms.model.enums.PaymentStructure;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MasterFee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_structure", length = 20, nullable = false)
    private PaymentStructure paymentStructure;
}
