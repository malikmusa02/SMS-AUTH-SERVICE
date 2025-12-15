package com.java.sms.serviceImpl;

import com.java.sms.DataClass.RoleDTO;
import com.java.sms.DataClass.TeacherDTO;
import com.java.sms.model.Role;
import com.java.sms.model.User;
import com.java.sms.openFeignClient.TeacherClient;
import com.java.sms.repository.RoleRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.response.TeacherResponseDTO;
import com.java.sms.service.TeacherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for managing Teacher Users.
 *
 * <p>This service handles:
 * <ul>
 *     <li>User creation (teacher users)</li>
 *     <li>Password encoding</li>
 *     <li>Role mapping</li>
 *     <li>Calling Django Teacher microservice via OpenFeign</li>
 *     <li>Transaction rollback on failure</li>
 * </ul>
 *
 * It ensures consistency between the Spring Boot User table
 * and the Django Teacher table.
 */
@Service
public class TeacherImpl implements TeacherService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TeacherClient teacherClient;
    private final RoleRepository roleRepository;


    public TeacherImpl(PasswordEncoder passwordEncoder, UserRepository userRepository, TeacherClient teacherClient, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.teacherClient = teacherClient;
        this.roleRepository = roleRepository;
    }



    /**
     * Creates a new Teacher User and also syncs the teacher record
     * with the external Django microservice.
     *
     * <p>This method performs the following steps:
     * <ol>
     *     <li>Checks for duplicate email</li>
     *     <li>Creates and saves User in DB</li>
     *     <li>Maps RoleDTO → Role entity</li>
     *     <li>Sends TeacherDTO to Django using OpenFeign</li>
     *     <li>Rolls back entire transaction on any failure</li>
     * </ol>
     *
     * @param request Teacher creation request containing user + teacher details
     * @return ResponseEntity (success or error response)
     */
    @Transactional
    @Override
    public ResponseEntity<?> createTeacherUser(TeacherDTO request) {
        try {

            // 1. Check duplicate email
            if (userRepository.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }

            User user = new User();
            user.setFirstName( request.getFirstName());
            user.setMiddleName(request.getMiddleName());
            user.setLastName( request.getLastName());
            user.setEmail( request.getEmail());
            user.setPassword(passwordEncoder.encode( request.getPassword()));
            user.setPhoneNo(request.getPhoneNo());
            user.setAadharNo(request.getAadharNo());
            user.setPanNo(request.getPanNo());
            user.setGender(request.getGender());
            user.setQualification(request.getQualification());

            // Convert RoleDTO → Role Entity Set
            Set<Role> roleSet = request.getRoles().stream()
                    .map(roleDTO -> roleRepository.findByName(roleDTO.getName().toLowerCase())
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleDTO.getName())))
                    .collect(Collectors.toSet());

            user.setRoles(roleSet);




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


            // Create teacher record in Django
            TeacherDTO teacherDTO = new TeacherDTO();
            teacherDTO.setUser(savedUser.getId());
            TeacherResponseDTO createdTeacher = teacherClient.createTeacher(teacherDTO);


            // If OfficeStaff creation failed (null or error)
            if (createdTeacher == null) {
                throw new RuntimeException("Failed to create OfficeStaff in Django service");
            }

            //  6. Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Office staff created successfully",
                    "userId", savedUser.getId(),
                    "officeStaff", createdTeacher
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