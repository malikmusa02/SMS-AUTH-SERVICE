package com.java.sms.controller;


import com.java.sms.DataClass.StudentDTO;
import com.java.sms.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;



    @PostMapping("/create")
    public ResponseEntity<?> createStudent(@RequestBody StudentDTO studentDTO){
        return studentService.createStudents(studentDTO);
    }


}
