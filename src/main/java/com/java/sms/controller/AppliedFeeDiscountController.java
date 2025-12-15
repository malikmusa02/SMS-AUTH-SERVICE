package com.java.sms.controller;


import com.java.sms.DataClass.ApplyDiscountRequest;
import com.java.sms.DataClass.UpdateDiscountRequest;
import com.java.sms.response.AppliedFeeDiscountResponse;
import com.java.sms.service.AppliedFeeDiscountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/AppliedFeeDiscount")
public class AppliedFeeDiscountController {


    private final AppliedFeeDiscountService service;

    public AppliedFeeDiscountController(AppliedFeeDiscountService service) {
        this.service = service;
    }


    @GetMapping
    public ResponseEntity<?> list(@RequestParam(value = "student_year_id", required = false) Long studentYearId) {

        return ResponseEntity.ok(service.list(studentYearId));

    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<?> apply(@Valid @RequestBody ApplyDiscountRequest req,
                                   java.security.Principal principal) {

        String username = principal != null ? principal.getName() : "system";

        AppliedFeeDiscountResponse resp = service.applyDiscount(req, username);

        return ResponseEntity.ok(resp);

    }


    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @Valid @RequestBody UpdateDiscountRequest req,
                                    java.security.Principal principal) {

        String username = principal != null ? principal.getName() : "system";

        AppliedFeeDiscountResponse resp = service.updateDiscount(id, req, username);

        return ResponseEntity.ok(resp);

    }
}
