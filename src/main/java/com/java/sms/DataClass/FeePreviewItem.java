package com.java.sms.DataClass;

import lombok.*;
import java.util.List;

/**
 * Preview item per month, each contains list of fees (brief).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePreviewItem {
    private String month;
    private List<FeeBrief> fees;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FeeBrief {
        private Long feeId;
        private String feeType;
        private String originalAmount;
        private String paidAmount;
        private String status;
        private String appliedDiscount;
    }
}
