package com.java.sms.model;

import com.java.sms.model.enums.FeeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "YearLevelFee")
public class YearLevelFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assuming YearLevel and FeeType are other entities (either in Spring or fetched from Django)
    private Long yearLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", length = 100, nullable = false)
    private FeeType feeType;


    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal amount;


}

