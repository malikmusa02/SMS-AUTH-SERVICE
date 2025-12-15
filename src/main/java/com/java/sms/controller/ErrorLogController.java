package com.java.sms.controller;



import com.java.sms.model.ErrorLog;
import com.java.sms.service.ErrorLogService;
import com.java.sms.repository.ErrorLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only endpoints for ErrorLog entries.
 * Admins / support can use these to inspect errors recorded by the application.
 */

@RestController
@RequestMapping("/error-logs")
@Slf4j
public class ErrorLogController {

    private final ErrorLogRepository repo;
    private final ErrorLogService service;

    public ErrorLogController(ErrorLogRepository repo, ErrorLogService service) {
        this.repo = repo;
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ErrorLog>> listAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ErrorLog> getById(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // optional create for admin/testing
    @PostMapping
    public ResponseEntity<ErrorLog> create(@RequestBody ErrorLog payload) {
        return ResponseEntity.status(201).body(service.save(payload));
    }
}
