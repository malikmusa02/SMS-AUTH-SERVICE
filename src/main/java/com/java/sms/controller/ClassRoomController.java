package com.java.sms.controller;




import com.java.sms.exception.ApiResponse;
import com.java.sms.model.ClassRoom;
import com.java.sms.service.ClassRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classrooms")
public class ClassRoomController {

    private final ClassRoomService service;

    public ClassRoomController(ClassRoomService service) {
        this.service = service;
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse> getAll() {
        List<ClassRoom> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Fetched Successfully", list));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Fetched Successfully", service.getById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestBody ClassRoom room) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Data Saved Successfully", service.create(room)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody ClassRoom room) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Data Updated Successfully", service.update(id, room)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Data Deleted", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }
}

