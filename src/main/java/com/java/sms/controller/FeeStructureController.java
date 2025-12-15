package com.java.sms.controller;


import com.java.sms.DataClass.FeeStructureRequest;
import com.java.sms.response.FeeStructureResponse;
import com.java.sms.service.FeeStructureService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/fee-structures")
public class FeeStructureController {

    private final FeeStructureService service;

    public FeeStructureController(FeeStructureService service) {
        this.service = service;
    }



    @PostMapping("/create")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeeStructureResponse> create(@Valid @RequestBody FeeStructureRequest req) {
        FeeStructureResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/fee-structures/" + created.getId())).body(created);
    }




    @PutMapping("/update/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeeStructureResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody FeeStructureRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }




    @GetMapping("/getById/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FeeStructureResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }



    @GetMapping("/getAll")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FeeStructureResponse>> list(@RequestParam(value = "year_level_id", required = false) Long yearLevelId) {
        if (yearLevelId != null) {
            return ResponseEntity.ok(service.findByYearLevelId(yearLevelId));
        } else {
            return ResponseEntity.ok(service.findAll());
        }
    }




    @DeleteMapping("/delete/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
