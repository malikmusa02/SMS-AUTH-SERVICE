package com.java.sms.controller;


import com.java.sms.DataClass.PaymentRequest;
import com.java.sms.response.PaymentResponse;
import com.java.sms.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;


    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse created = service.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PaymentResponse> update(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        PaymentResponse updated = service.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * List payments with optional filters and pagination.
     * Example: /api/payments?page=0&size=20&sort=paymentDate,desc&status=PENDING&method=ONLINE
     */
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "paymentDate,desc") String sort
    ) {
        Sort sortObj = Sort.by(Sort.Order.desc("paymentDate"));
        // parse sort param if provided like "field,asc"
        if (sort != null && sort.contains(",")) {
            String[] s = sort.split(",");
            sortObj = Sort.by(new Sort.Order(Sort.Direction.fromString(s[1]), s[0]));
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<PaymentResponse> result = service.list(status, method, pageable);
        return ResponseEntity.ok(result);
    }


}

