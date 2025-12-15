package com.java.sms.controller;


import com.java.sms.DataClass.UserDTO;
import com.java.sms.DataClass.OfficeStaffDTO;
import com.java.sms.service.OfficeStaffService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/office-staff")
public class OfficeStaffController {

    private final OfficeStaffService officeStaffService;

    public OfficeStaffController(OfficeStaffService officeStaffService) {

        this.officeStaffService = officeStaffService;
    }


    //  Create Office Staff + User (from JSON body)
    @PreAuthorize("hasRole('director')")
//    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    @PostMapping("/create")
    public ResponseEntity<?> createOfficeStaff(@Valid @RequestBody OfficeStaffDTO request) {
        return officeStaffService.createOfficeStaffUser(request);
    }

    }

//    //  Fetch Office Staff by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<OfficeStaffDTO> getOfficeStaffById(@PathVariable Long id) {
//        return ResponseEntity.ok(officeStaffService.getOfficeStaffById(id));
//    }




