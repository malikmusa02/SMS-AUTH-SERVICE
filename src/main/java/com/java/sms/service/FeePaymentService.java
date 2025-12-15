package com.java.sms.service;


import com.java.sms.DataClass.FeePaymentRequest;
import com.java.sms.response.FeePaymentResponse;

import java.util.List;

public interface FeePaymentService {

    FeePaymentResponse create(FeePaymentRequest request);

    FeePaymentResponse update(Long id, FeePaymentRequest request);

    FeePaymentResponse getById(Long id);

    List<FeePaymentResponse> getAll();

    void delete(Long id);
}
