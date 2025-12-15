package com.java.sms.DataClass;


import com.java.sms.model.enums.PaymentMethod;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitFeeRequest {

    @NotNull
    private Long studentYearId;

    private Long schoolYearId;

    @NotNull
    private PaymentMethod paymentMethod; // "online", "cash", "cheque" etc

    private String chequeNumber; // optional when cheque

    /**
     * List of fee maps with keys similar to your Django payload:
     * - fee_type_id or fee_id (Long)
     * - month (Integer)
     * - amount (BigDecimal or numeric)
     * - due_date (optional string "yyyy-MM-dd")
     */
    @NotNull
    private List<Map<String,Object>> fees;

}

