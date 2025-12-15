package com.java.sms.service;



import com.java.sms.DataClass.*;
import com.java.sms.response.ConfirmPaymentResponse;
import com.java.sms.response.InitiatePaymentResponse;

import java.util.List;
import java.util.Map;

public interface StudentFeeService {
    Object createOrUpdateStudentFee(StudentFeeRequest req);

    List<FeePreviewItem> previewFees(Long studentYearId);

    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest req) throws Exception;

    ConfirmPaymentResponse confirmPayment(ConfirmPaymentRequest req);




    // in com.java.sms.service.StudentFeeService
    Map<String, Object> getStudentUnpaidFees();

    List<Map<String,Object>> getOverdueFees(Long studentYearId, Integer month, Long schoolYearId);

    Object submitFee(SubmitFeeRequest req) throws Exception;

    List<Map<String,Object>> getFeeHistory(Long studentYearId, Long schoolYearId);

    List<Map<String,Object>> getPendingFees(Long schoolYearId);

}
