package com.java.sms.service;

import com.java.sms.DataClass.StudentDTO;
import org.springframework.http.ResponseEntity;

public interface StudentService {
    ResponseEntity<?> createStudents(StudentDTO studentDTO);
}
