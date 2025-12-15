package com.java.sms.mapper;



import com.java.sms.DataClass.PaymentRequest;
import com.java.sms.model.enums.PaymentMethod;
import com.java.sms.model.enums.PaymentStatus;
import com.java.sms.response.PaymentResponse;
import com.java.sms.model.Payment;

import java.time.LocalDateTime;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment p) {
        if (p == null) return null;
        return PaymentResponse.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .remarks(p.getRemarks())
                .paymentDate(p.getPaymentDate())
                .chequeNumber(p.getChequeNumber())
                .payoutId(p.getPayoutId())
                .fundAccountId(p.getFundAccountId())
                .contactId(p.getContactId())
                .build();
    }

    public static void updateEntityFromRequest(PaymentRequest req, Payment entity) {
        entity.setAmount(req.getAmount());
        entity.setPaymentMethod(req.getPaymentMethod() != null
                ? PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase())
                : null);
        entity.setStatus(req.getStatus() != null
                ? PaymentStatus.valueOf(req.getStatus().toUpperCase())
                : null);
        entity.setRemarks(req.getRemarks());
        entity.setPaymentDate(req.getPaymentDate());
        entity.setChequeNumber(req.getChequeNumber());
        entity.setPayoutId(req.getPayoutId());
        entity.setFundAccountId(req.getFundAccountId());
        entity.setContactId(req.getContactId());
    }


    public static Payment toEntity(PaymentRequest req) {
        Payment p = new Payment();
        updateEntityFromRequest(req, p);
        return p;
    }


}
