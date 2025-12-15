package com.java.sms.controller;


import com.java.sms.DataClass.MasterFeeRequest;
import com.java.sms.response.MasterFeeResponse;
import com.java.sms.service.MasterFeeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master-fees")
public class MasterFeeController {

    private final MasterFeeService service;

    public MasterFeeController(MasterFeeService service) {
        this.service = service;
    }


    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MasterFeeResponse> create(@Valid @RequestBody MasterFeeRequest req) {

        System.out.println("Incoming paymentStructure -> '" + req.getPaymentStructure());
        MasterFeeResponse created = service.create(req);
        return ResponseEntity.ok(created);
    }




    @PutMapping("/update/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MasterFeeResponse> update(@PathVariable Long id, @Valid @RequestBody MasterFeeRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }




    @GetMapping("/getById/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MasterFeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }




    @GetMapping("/getAll")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MasterFeeResponse>> list() {
        return ResponseEntity.ok(service.findAll());
    }




    @DeleteMapping("/delete/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }



}

