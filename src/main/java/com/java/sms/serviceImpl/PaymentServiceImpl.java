package com.java.sms.serviceImpl;


import com.java.sms.DataClass.PaymentRequest;
import com.java.sms.exception.ApiException;
import com.java.sms.mapper.PaymentMapper;
import com.java.sms.model.Payment;
import com.java.sms.model.enums.PaymentMethod;
import com.java.sms.model.enums.PaymentStatus;
import com.java.sms.repository.PaymentRepository;
import com.java.sms.response.PaymentResponse;
import com.java.sms.service.PaymentService;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repo;

    public PaymentServiceImpl(PaymentRepository repo) {
        this.repo = repo;
    }

    @Override
    public PaymentResponse create(PaymentRequest request) {
        // basic business validations
        validateRequest(request, true);

        // map and save
        Payment p = PaymentMapper.toEntity(request);
        Payment saved = repo.save(p);
        return PaymentMapper.toResponse(saved);
    }

    @Override
    public PaymentResponse update(Long id, PaymentRequest request) {

        Payment existing = repo.findById(id).orElseThrow(() ->
                new ApiException("Payment not found: " + id, HttpStatus.NOT_FOUND));


        validateRequest(request, false);
        PaymentMapper.updateEntityFromRequest(request, existing);
        Payment updated = repo.save(existing);
        return PaymentMapper.toResponse(updated);
    }

    @Override
    public PaymentResponse findById(Long id) {
        Payment p = repo.findById(id).orElseThrow(() -> new ApiException("Payment not found: " + id, HttpStatus.NOT_FOUND));
        return PaymentMapper.toResponse(p);
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ApiException("Payment not found: " + id, HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
    }

    @Override
    public Page<PaymentResponse> list(String status, String method, Pageable pageable) {

        Page<Payment> page;

        if (status != null && method != null) {

            page = repo.findByStatusAndPaymentMethod(
                    PaymentStatus.valueOf(status.toUpperCase(Locale.ROOT)),
                    PaymentMethod.valueOf(method.toUpperCase(Locale.ROOT)),
                    pageable
            );


        } else if (status != null) {

            page = repo.findByStatus(PaymentStatus.valueOf(status.toUpperCase(Locale.ROOT)), pageable);

        } else if (method != null) {

            page = repo.findByPaymentMethod(PaymentMethod.valueOf(method.toUpperCase(Locale.ROOT)), pageable);

        } else {

            page = repo.findAll(pageable);
        }
        return page.map(PaymentMapper::toResponse);
    }






    // business rules and validations
    private void validateRequest(PaymentRequest req, boolean creating) {
        // ensure enums are valid

        try {
            PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new ApiException("Invalid paymentMethod: " + req.getPaymentMethod(), HttpStatus.BAD_REQUEST);
        }


        try {
           PaymentStatus.valueOf(req.getStatus().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new ApiException("Invalid status: " + req.getStatus(), HttpStatus.BAD_REQUEST);
        }


        // if cheque, chequeNumber required and unique
        if (req.getPaymentMethod().equalsIgnoreCase("CHEQUE")
                || req.getPaymentMethod().equalsIgnoreCase("Cheque")) {


            if (req.getChequeNumber() == null || req.getChequeNumber().isBlank()) {
                throw new ApiException("chequeNumber is required for cheque payments", HttpStatus.BAD_REQUEST);
            }

            if (creating && repo.existsByChequeNumber(req.getChequeNumber())) {
                throw new ApiException("chequeNumber already used",HttpStatus.CONFLICT);
            }


        }

    }





}

