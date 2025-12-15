package com.java.sms.response;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterFeeResponse {
    private Long id;
    private String paymentStructure;
}
