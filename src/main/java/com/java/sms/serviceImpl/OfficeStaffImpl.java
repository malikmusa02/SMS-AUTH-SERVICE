package com.java.sms.serviceImpl;

import com.java.sms.DataClass.OfficeStaffDTO;


import com.java.sms.model.Role;
import com.java.sms.model.User;
import com.java.sms.openFeignClient.OfficeStaffClient;
import com.java.sms.repository.RoleRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.service.OfficeStaffService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for managing Office Staff creation.
 *
 * <p>This service does the following:
 * <ul>
 *     <li>Creates a new User entity for office staff</li>
 *     <li>Assigns appropriate roles</li>
 *     <li>Calls Django service via Feign Client to create OfficeStaff record</li>
 *     <li>Ensures transactional consistency between services</li>
 * </ul>
 *
 * <p>If any step fails, the entire transaction is rolled back to maintain data integrity.
 */
@Service
public class OfficeStaffImpl implements OfficeStaffService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OfficeStaffClient officeStaffClient;
    private final RoleRepository roleRepository;


    public OfficeStaffImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, OfficeStaffClient officeStaffClient, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.officeStaffClient = officeStaffClient;
        this.roleRepository = roleRepository;
    }



    /**
     * Creates a new office staff user along with a remote OfficeStaff record.
     *
     * <p>Steps:
     * <ol>
     *     <li>Validate duplicate email</li>
     *     <li>Create and save User entity</li>
     *     <li>Assign roles from DTO</li>
     *     <li>Call Django service to create OfficeStaff</li>
     *     <li>Return success response</li>
     * </ol>
     *
     * <p>If any part of the process fails, the transaction is rolled back.
     *
     * @param request Incoming office staff form input
     * @return Success or error response
     */
    @Transactional
    @Override
    public ResponseEntity<?> createOfficeStaffUser(OfficeStaffDTO request) {
        try {
            // 1. Check duplicate email
            if (userRepository.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }

            // 2. Create user entity
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setMiddleName(request.getMiddleName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setGender(request.getGender());
            user.setPanNo(request.getPanNo());
            user.setQualification(request.getQualification());
            user.setAadharNo(request.getAadharNo());
            user.setPhoneNo(request.getPhoneNo());

            // 3. Assign roles
            Set<Role> roleSet = request.getRoles().stream()
                    .map(roleDTO -> roleRepository.findByName(roleDTO.getName().toLowerCase())
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleDTO.getName())))
                    .collect(Collectors.toSet());
            user.setRoles(roleSet);

            // 4. Save user
            User savedUser = userRepository.save(user);

            // 5. Create OfficeStaff record (remote API call)
            OfficeStaffDTO officeStaffDTO = new OfficeStaffDTO();
            officeStaffDTO.setUser(savedUser.getId());
            OfficeStaffDTO createdOfficeStaff = officeStaffClient.createOfficeStaff(officeStaffDTO);

            // If OfficeStaff creation failed (null or error)
            if (createdOfficeStaff == null) {
                throw new RuntimeException("Failed to create OfficeStaff in Django service");
            }

            //  6. Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Office staff created successfully",
                    "userId", savedUser.getId(),
                    "officeStaff", createdOfficeStaff
            ));

        } catch (Exception e) {
            //  Rollback the transaction if any failure occurs
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Transaction rolled back",
                            "message", e.getMessage()
                    ));
        }
    }
}
