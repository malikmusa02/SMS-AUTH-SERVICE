package com.java.sms.controller;


import com.java.sms.DataClass.UserDTO;
import com.java.sms.DataClass.TeacherDTO;
import com.java.sms.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

//    // Create Teacher + User (from JSON body)
//    @PreAuthorize("hasRole('director')")
//    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
//    public ResponseEntity<?> createTeacher(@ModelAttribute TeacherDTO request) {
//        return teacherService.createTeacherUser(request);
//    }


    // Create Teacher + User (from JSON body)
    @PreAuthorize("hasRole('director')")
    @PostMapping("/create")
    public ResponseEntity<?> createTeacher(@Valid @RequestBody TeacherDTO request) {
        return teacherService.createTeacherUser(request);
    }

//    // Fetch Teacher by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<TeacherDTO> getTeacherById(@PathVariable Long id) {
//        return ResponseEntity.ok(teacherService.getTeacherById(id));
//    }
}

