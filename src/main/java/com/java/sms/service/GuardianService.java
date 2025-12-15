package com.java.sms.service;


import com.java.sms.DataClass.GuardianDTO;
import org.springframework.http.ResponseEntity;

public interface GuardianService {
    ResponseEntity<?> createGuardian(GuardianDTO guardianDTO);
}
