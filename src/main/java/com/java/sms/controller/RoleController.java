package com.java.sms.controller;



import com.java.sms.DataClass.RoleDTO;
import com.java.sms.model.Role;
import com.java.sms.response.RoleResponse;
import com.java.sms.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // CREATE
    @PreAuthorize("hasRole('director')")
    @PostMapping("/create")
    public ResponseEntity<RoleResponse> createRole(@RequestBody RoleDTO role) {
        RoleResponse createdRole = roleService.createRole(role);
        return ResponseEntity.ok(createdRole);
    }

    // READ ALL
    @GetMapping("/getAllRole")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // READ BY ID
    @GetMapping("/getById/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @RequestBody RoleDTO role) {
        return ResponseEntity.ok(roleService.updateRole(id, role));
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok("Role deleted successfully");
    }
}

