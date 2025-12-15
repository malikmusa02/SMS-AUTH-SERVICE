package com.java.sms.serviceImpl;


import com.java.sms.DataClass.GuardianDTO;
import com.java.sms.DataClass.StudentDTO;
import com.java.sms.model.Role;
import com.java.sms.model.User;
import com.java.sms.openFeignClient.GuardianClient;
import com.java.sms.openFeignClient.StudentClient;
import com.java.sms.repository.RoleRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.service.GuardianService;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
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
 * Service implementation for handling Guardian creation business logic.
 *
 * <p>This service performs the following operations:
 * <ul>
 *     <li>Validates duplicate email</li>
 *     <li>Converts {@link GuardianDTO} into {@link User}</li>
 *     <li>Assigns roles to the user</li>
 *     <li>Saves user in local database</li>
 *     <li>Creates Guardian entry in Django service through OpenFeign</li>
 *     <li>Ensures atomicity using Spring Transactions</li>
 * </ul>
 *
 * <p>In case of any error during the process, the transaction is rolled back and
 * appropriate error response is returned.
 */
@Service
@RequiredArgsConstructor
public class GuardianServiceImpl implements GuardianService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final GuardianClient guardianClient;


    /**
     * Creates a guardian along with a user record in a transactional manner.
     *
     * <p>This method:
     * <ol>
     *     <li>Validates email uniqueness</li>
     *     <li>Converts GuardianDTO to User entity</li>
     *     <li>Assigns roles to the user</li>
     *     <li>Saves the user in local DB</li>
     *     <li>Passes userId to Django Guardian Service (via Feign client)</li>
     *     <li>Returns success response if everything goes well</li>
     * </ol>
     *
     * <p>If any exception occurs:
     * <ul>
     *     <li>The entire transaction is rolled back</li>
     *     <li>An {@link ApiException} or an internal server error response is returned</li>
     * </ul>
     *
     * @param request Guardian details required for account creation
     * @return A ResponseEntity containing either success or error response
     */
    @Transactional
    @Override
    public ResponseEntity<?> createGuardian(GuardianDTO guardianDTO) {


        try {
            // 1. Check duplicate email
            if (userRepository.findByEmail(guardianDTO.getEmail().toLowerCase()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }

            //  2. Create new user
            User user = new User();
            user.setFirstName(guardianDTO.getFirstName());
            user.setMiddleName(guardianDTO.getMiddleName());
            user.setLastName(guardianDTO.getLastName());
            user.setEmail(guardianDTO.getEmail());
            user.setPassword(passwordEncoder.encode(guardianDTO.getPassword()));
            user.setAadharNo(guardianDTO.getAadharNo());
            user.setGender(guardianDTO.getGender());
            user.setPanNo(guardianDTO.getPanNo());
            user.setPhoneNo(guardianDTO.getPhoneNo());
            user.setQualification(guardianDTO.getQualification());
            user.setActive(true);

            //  3. Set roles (if any)
            // Convert RoleDTO → Role Entity Set
            Set<Role> roleSet = guardianDTO.getRoles().stream()
                    .map(roleDTO -> roleRepository.findByName(roleDTO.getName().toLowerCase())
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleDTO.getName())))
                    .collect(Collectors.toSet());

            // Save user image
            // Create directory path
//            String fullPath = System.getProperty("user.dir") + "/" + UPLOAD_DIR;
//
//            // Create folder if not exists
//            File folder = new File(fullPath);
//            if (!folder.exists()) folder.mkdirs();
//
//            // Save image if provided
//            if (request.getImage() != null && !request.getImage().isEmpty()) {
//
//                String filename = UUID.randomUUID() + "_" + request.getImage().getOriginalFilename();
//                Path path = Paths.get(fullPath + filename);
//
//                Files.copy(request.getImage().getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//                //Store public image URL in DB
//                String imageUrl = baseUrl + "/uploads/profile_pics/" + filename;
//                user.setImagePath(imageUrl);
//            }


            User savedUser = userRepository.save(user);

            GuardianDTO userGuardian = new GuardianDTO();
            userGuardian.setUser(savedUser.getId());
            GuardianDTO createdGuardian = guardianClient.creteGuardian(userGuardian);
            // If OfficeStaff creation failed (null or error)
            if (createdGuardian == null) {
                throw new RuntimeException("Failed to create OfficeStaff in Django service");
            }

            //  6. Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Office staff created successfully",
                    "userId", savedUser.getId(),
                    "officeStaff", createdGuardian
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
