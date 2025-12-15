package com.java.sms.controller;


import com.java.sms.model.UserStatusLog;
import com.java.sms.repository.UserStatusLogRepository;
import com.java.sms.service.UserStatusLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD controller for UserStatusLog.
 * Mirrors the Django ModelViewSet behavior (create, read, update, delete).
 */


@RestController
@RequestMapping("/user-status-logs")
@Slf4j
public class UserStatusLogController {

    private final UserStatusLogService service;

    public UserStatusLogController(UserStatusLogService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UserStatusLog>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserStatusLog> getById(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserStatusLog> create(@RequestBody UserStatusLog payload) {
        return ResponseEntity.status(201).body(service.save(payload));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStatusLog> update(@PathVariable Long id, @RequestBody UserStatusLog payload) {
        return service.findById(id).map(existing -> {
            existing.setUser(payload.getUser());
            existing.setStatus(payload.getStatus());
            existing.setReason(payload.getReason());
            UserStatusLog updated = service.save(existing);
            return ResponseEntity.ok(updated);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
