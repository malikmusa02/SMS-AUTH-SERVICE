package com.java.sms.service;

import com.java.sms.DataClass.OfficeStaffDTO;
import com.java.sms.DataClass.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


public interface OfficeStaffService {

    ResponseEntity<?> createOfficeStaffUser(OfficeStaffDTO request);

}
