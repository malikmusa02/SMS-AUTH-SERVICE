package com.java.sms.model;


import com.java.sms.model.enums.FeeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import lombok.*;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;


@Entity
@Table(name = "fee_structure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link to MasterFee (local FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_fee_id", nullable = false)
    private MasterFee masterFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", length = 100, nullable = false)
    private FeeType feeType;

    @Column(name = "fee_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal feeAmount;

    /**
     * Year-level IDs from Django:
     * We store the list of YearLevel IDs locally as a collection of Longs.
     * We do NOT create a foreign-key or join table to a YearLevel entity here,
     * because YearLevel is owned by Django.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fee_structure_yearlevel_ids", joinColumns = @JoinColumn(name = "fee_structure_id"))
    @Column(name = "year_level_id")
    private Set<Long> yearLevelIds;
}

