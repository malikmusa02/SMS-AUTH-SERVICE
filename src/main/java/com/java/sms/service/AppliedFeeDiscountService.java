package com.java.sms.service;



import com.java.sms.DataClass.ApplyDiscountRequest;
import com.java.sms.DataClass.UpdateDiscountRequest;
import com.java.sms.response.AppliedFeeDiscountResponse;

import java.util.List;
import java.util.Map;

public interface AppliedFeeDiscountService {

    /**
     * If studentYearId provided, returns available fees and applied discounts (like Django)
     * otherwise returns all applied discounts.
     */
    Object list(Long studentYearId);

    List<Map<String, Object>> getAvailableFees(Long studentYearId);

    List<AppliedFeeDiscountResponse> getAppliedDiscounts(Long studentYearId);

    AppliedFeeDiscountResponse applyDiscount(ApplyDiscountRequest req, String currentUsername);

    AppliedFeeDiscountResponse updateDiscount(Long id, UpdateDiscountRequest req, String currentUsername);
}
