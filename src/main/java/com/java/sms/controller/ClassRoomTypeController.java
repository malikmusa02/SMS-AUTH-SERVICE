package com.java.sms.controller;

import com.java.sms.exception.ApiResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.java.sms.model.ClassRoomType;
import com.java.sms.service.ClassRoomTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classroom-types")
public class ClassRoomTypeController {

    private final ClassRoomTypeService service;

    public ClassRoomTypeController(ClassRoomTypeService service) {
        this.service = service;
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse> getAll() {
        List<ClassRoomType> list = service.getAll();
        return ResponseEntity.ok(ApiResponse.success("Fetched Successfully", list));
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Fetched Successfully", service.getById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestBody ClassRoomType type) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Data Saved Successfully", service.create(type)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Long id, @RequestBody ClassRoomType type) {
        try {
            return ResponseEntity.ok(ApiResponse.success("Data Updated Successfully", service.update(id, type)));
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

