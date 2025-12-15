package com.java.sms.controller;


import com.java.sms.DataClass.FeePaymentRequest;
import com.java.sms.response.FeePaymentResponse;
import com.java.sms.service.FeePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fee-payments")
@RequiredArgsConstructor
public class FeePaymentController {

    private final FeePaymentService service;



    @PostMapping("/create")
    public ResponseEntity<FeePaymentResponse> create(@Valid @RequestBody FeePaymentRequest req) {
        return ResponseEntity.status(201).body(service.create(req));
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<FeePaymentResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody FeePaymentRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }


    @GetMapping("/getById/{id}")
    public ResponseEntity<FeePaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }


    @GetMapping("/getAll")
    public ResponseEntity<List<FeePaymentResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
