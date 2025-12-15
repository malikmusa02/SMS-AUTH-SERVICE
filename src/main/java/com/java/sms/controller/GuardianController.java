package com.java.sms.controller;


import com.java.sms.DataClass.GuardianDTO;
import com.java.sms.service.GuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guardian")
@RequiredArgsConstructor
public class GuardianController {

    private final GuardianService guardianService;


    @PostMapping("/create")
    public ResponseEntity<?> createGuardian(@RequestBody GuardianDTO guardianDTO){
        return guardianService.createGuardian(guardianDTO);
    }
}
