package com.java.sms.service;

import com.java.sms.DataClass.TeacherDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


public interface TeacherService {

    ResponseEntity<?> createTeacherUser(TeacherDTO request);
}
