package com.java.sms.controller;


import com.java.sms.DataClass.EmployeeRequest;
import com.java.sms.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Employee.
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    /** GET /employees/get_emp */
    @GetMapping("/get_emp")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getEmployees(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(service.listEmployees(role, id, name));
    }

    /** POST /employees/create_emp */
    @PostMapping("/create_emp")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(201).body(
                service.createEmployee(request)
        );
    }

    /** PUT /employees/update_emp */
    @PutMapping("/update_emp")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateEmployee(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(service.updateEmployee(request));
    }

    /** DELETE /employees/{id} */
    @DeleteMapping("/{id}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        service.deleteEmployee(id);
        return ResponseEntity.ok(
                java.util.Map.of("message", "Employee deleted successfully")
        );
    }
}
