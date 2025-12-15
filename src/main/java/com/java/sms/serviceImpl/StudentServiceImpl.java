package com.java.sms.serviceImpl;

import com.java.sms.DataClass.StudentDTO;
import com.java.sms.DataClass.TeacherDTO;
import com.java.sms.model.Role;
import com.java.sms.model.User;
import com.java.sms.openFeignClient.StudentClient;
import com.java.sms.repository.RoleRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.response.TeacherResponseDTO;
import com.java.sms.service.StudentService;
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
 * Service implementation for managing Student Users.
 *
 * <p>This service:
 * <ul>
 *     <li>Validates student email duplication</li>
 *     <li>Creates a new User record</li>
 *     <li>Maps RoleDTO → Role entities</li>
 *     <li>Calls Django Student microservice using OpenFeign</li>
 *     <li>Ensures full transaction rollback on any failure</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentClient studentClient;



    /**
     * Creates a new student user in the Spring Boot database
     * and then sends student data to the Django Student service.
     *
     * <p>Steps performed:
     * <ol>
     *     <li>Check for duplicate email</li>
     *     <li>Create and save User entity</li>
     *     <li>Assign roles</li>
     *     <li>Send student record via OpenFeign</li>
     *     <li>Rollback on any failure</li>
     * </ol>
     *
     * @param studentDTO Incoming request containing student + user data
     * @return ResponseEntity with success or error response
     */
    @Transactional
    @Override
    public ResponseEntity<?> createStudents(StudentDTO studentDTO) {

        try {
            // 1. Check duplicate email
            if (userRepository.findByEmail(studentDTO.getEmail().toLowerCase()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }

            //  2. Create new user
            User user = new User();
            user.setFirstName(studentDTO.getFirstName());
            user.setMiddleName(studentDTO.getMiddleName());
            user.setLastName(studentDTO.getLastName());
            user.setEmail(studentDTO.getEmail());
            user.setPassword(passwordEncoder.encode(studentDTO.getPassword()));
            user.setAadharNo(studentDTO.getAadharNo());
            user.setGender(studentDTO.getGender());
            user.setPanNo(studentDTO.getPanNo());
            user.setPhoneNo(studentDTO.getPhoneNo());
            user.setQualification(studentDTO.getQualification());
            user.setActive(true);

            //  3. Set roles (if any)
            // Convert RoleDTO → Role Entity Set
            Set<Role> roleSet = studentDTO.getRoles().stream()
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

            StudentDTO userStudent = new StudentDTO();
            userStudent.setUser(savedUser.getId());
            StudentDTO createdStudent = studentClient.createStudent(userStudent);
            // If students creation failed (null or error)
            if (createdStudent == null) {
                throw new RuntimeException("Failed to create OfficeStaff in Django service");
            }

            //  6. Return success response
            return ResponseEntity.ok(Map.of(
                    "message", "Office staff created successfully",
                    "userId", savedUser.getId(),
                    "officeStaff", createdStudent
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
