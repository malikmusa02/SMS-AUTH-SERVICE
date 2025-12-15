package com.java.sms.serviceImpl;


import com.java.sms.DataClass.FeePaymentRequest;
import com.java.sms.exception.ApiException;
import com.java.sms.exception.ResourceNotFoundException;
import com.java.sms.mapper.FeePaymentMapper;
import com.java.sms.model.FeePayment;
import com.java.sms.model.StudentFee;
import com.java.sms.model.enums.PaymentMethod;
import com.java.sms.model.enums.PaymentStatus;
import com.java.sms.repository.FeePaymentRepository;
import com.java.sms.repository.StudentFeeRepository;
import com.java.sms.response.FeePaymentResponse;
import com.java.sms.service.FeePaymentService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class FeePaymentServiceImpl implements FeePaymentService {

    private final FeePaymentRepository feePaymentRepo;
    private final StudentFeeRepository studentFeeRepo;

    public FeePaymentServiceImpl(FeePaymentRepository feePaymentRepo,StudentFeeRepository studentFeeRepo
                                 ) {
        this.feePaymentRepo = feePaymentRepo;
        this.studentFeeRepo = studentFeeRepo;
    }

    @Override
    public FeePaymentResponse create(FeePaymentRequest req) {

        StudentFee studentFee = studentFeeRepo.findById(req.getStudentFeeId())
                .orElseThrow(() -> new ApiException("StudentFee not found",HttpStatus.NOT_FOUND));

        FeePayment payment = FeePayment.builder()
                .studentFee(studentFee)
                .amount(req.getAmount())
                .paymentMethod(PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase()))
                .status(PaymentStatus.valueOf(req.getStatus().toUpperCase()))
                .notes(req.getNotes())
                .paymentDate(req.getPaymentDate())
                .chequeNumber(req.getChequeNumber())
                .razorpayPaymentId(req.getRazorpayPaymentId())
                .razorpayOrderId(req.getRazorpayOrderId())
                .razorpaySignature(req.getRazorpaySignature())
                .build();

        return FeePaymentMapper.toResponse(feePaymentRepo.save(payment));

    }

    @Override
    public FeePaymentResponse update(Long id, FeePaymentRequest req) {

        FeePayment existing = feePaymentRepo.findById(id)
                .orElseThrow(() -> new ApiException("FeePayment not found", HttpStatus.NOT_FOUND));

        existing.setAmount(req.getAmount());
        existing.setPaymentMethod(PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase()));
        existing.setStatus(PaymentStatus.valueOf(req.getStatus().toUpperCase()));
        existing.setNotes(req.getNotes());
        existing.setPaymentDate(req.getPaymentDate());
        existing.setChequeNumber(req.getChequeNumber());
        existing.setRazorpayPaymentId(req.getRazorpayPaymentId());
        existing.setRazorpayOrderId(req.getRazorpayOrderId());
        existing.setRazorpaySignature(req.getRazorpaySignature());

        return FeePaymentMapper.toResponse(feePaymentRepo.save(existing));
    }

    @Override
    public FeePaymentResponse getById(Long id) {
        return feePaymentRepo.findById(id)
                .map(FeePaymentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("FeePayment not found"));
    }

    @Override
    public List<FeePaymentResponse> getAll() {
        return feePaymentRepo.findAll()
                .stream()
                .map(FeePaymentMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!feePaymentRepo.existsById(id)) {
            throw new ResourceNotFoundException("FeePayment not found");
        }
        feePaymentRepo.deleteById(id);
    }
}
