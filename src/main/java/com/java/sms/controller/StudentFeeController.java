package com.java.sms.controller;


import com.java.sms.DataClass.ConfirmPaymentRequest;
import com.java.sms.DataClass.FeePreviewItem;
import com.java.sms.DataClass.InitiatePaymentRequest;
import com.java.sms.DataClass.StudentFeeRequest;
import com.java.sms.response.ConfirmPaymentResponse;
import com.java.sms.response.InitiatePaymentResponse;
import com.java.sms.service.StudentFeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.java.sms.DataClass.SubmitFeeRequest;
import java.util.List;
import java.util.Map;

/**
 * Controller exposing endpoints:
 * - POST   /student-fees         -> create/update single StudentFee (non-submit path)
 * - GET    /student-fees/fee_preview?student_year_id=...  -> preview
 * - POST   /student-fees/initiate_payment
 * - POST   /student-fees/confirm_payment
 *
 * Secured with @PreAuthorize("isAuthenticated()")
 */
@RestController
@RequestMapping("/student-fees")
public class StudentFeeController {

    private final StudentFeeService service;

    public StudentFeeController(StudentFeeService service) {
        this.service = service;
    }

    @PostMapping
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createOrUpdate(@Valid @RequestBody StudentFeeRequest req) {
        Object out = service.createOrUpdateStudentFee(req);
        return ResponseEntity.status(201).body(out);
    }

    @GetMapping("/fee_preview")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FeePreviewItem>> preview(@RequestParam("student_year_id") Long studentYearId) {
        List<FeePreviewItem> out = service.previewFees(studentYearId);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/initiate_payment")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(@RequestBody InitiatePaymentRequest req) throws Exception {
        return ResponseEntity.ok(service.initiatePayment(req));
    }

    @PostMapping("/confirm_payment")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConfirmPaymentResponse> confirmPayment(@RequestBody ConfirmPaymentRequest req) {
        return ResponseEntity.status(201).body(service.confirmPayment(req));
    }



    @GetMapping("/student_unpaid_fees")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> studentUnpaidFees() {
        Map<String, Object> out = service.getStudentUnpaidFees();
        return ResponseEntity.ok(out);
    }



    @GetMapping("/overdue_fees")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String,Object>>> overdueFees(
            @RequestParam(value = "student_year_id", required = false) Long studentYearId,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "school_year_id", required = false) Long schoolYearId
    ) {
        List<Map<String,Object>> out = service.getOverdueFees(studentYearId, month, schoolYearId);
        return ResponseEntity.ok(out);
    }





    @PostMapping("/submit_fee")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitFee(@RequestBody @Valid SubmitFeeRequest req) throws Exception {
        // returns initiate payment result if payment_mode == online, else created records
        Object out = service.submitFee(req);
        return ResponseEntity.status(201).body(out);
    }




    @GetMapping("/fee_history")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String,Object>>> feeHistory(
            @RequestParam("student_year_id") Long studentYearId,
            @RequestParam("school_year_id") Long schoolYearId
    ) {
        List<Map<String,Object>> out = service.getFeeHistory(studentYearId, schoolYearId);
        return ResponseEntity.ok(out);
    }

    @GetMapping("/pending_fees")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String,Object>>> pendingFees(@RequestParam("school_year_id") Long schoolYearId) {
        List<Map<String,Object>> out = service.getPendingFees(schoolYearId);
        return ResponseEntity.ok(out);
    }



}
