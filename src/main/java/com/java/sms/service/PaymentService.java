package com.java.sms.service;



import com.java.sms.DataClass.PaymentRequest;
import com.java.sms.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PaymentResponse create(PaymentRequest request);

    PaymentResponse update(Long id, PaymentRequest request);

    PaymentResponse findById(Long id);

    void delete(Long id);

    Page<PaymentResponse> list(String status, String method, Pageable pageable);


}
