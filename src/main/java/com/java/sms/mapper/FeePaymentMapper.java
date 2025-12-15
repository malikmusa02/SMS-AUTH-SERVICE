package com.java.sms.mapper;


import com.java.sms.model.FeePayment;
import com.java.sms.response.FeePaymentResponse;

public final class FeePaymentMapper {

    public static FeePaymentResponse toResponse(FeePayment p) {
        return FeePaymentResponse.builder()
                .id(p.getId())
                .studentFeeId(p.getStudentFee() != null ? p.getStudentFee().getId() : null)
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod().name())
                .status(p.getStatus().name())
                .notes(p.getNotes())
                .paymentDate(p.getPaymentDate())
                .chequeNumber(p.getChequeNumber())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpaySignature(p.getRazorpaySignature())
                .createdAt(p.getCreatedAt())
                .build();
    }
}

