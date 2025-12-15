package com.java.sms.response;


import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeStructureResponse {
    private Long id;
    private Long masterFeeId;
    private String feeType;
    private BigDecimal feeAmount;
    private Set<Long> yearLevelIds;
}
