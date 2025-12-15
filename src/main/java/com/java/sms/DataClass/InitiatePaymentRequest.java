package com.java.sms.DataClass;


import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Request shape for initiate_payment endpoint. 'fees' list items match Django dynamic payload.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentRequest {
    private Long studentYearId;
    // each map may contain keys: fee_id or fee_type_id, month, amount or paid_amount
    private List<Map<String, Object>> fees;
    private Long schoolYearId;
}
