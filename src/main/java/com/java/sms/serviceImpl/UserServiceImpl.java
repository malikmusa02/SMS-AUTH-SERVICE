package com.java.sms.serviceImpl;

import com.java.sms.DataClass.RoleDTO;
import com.java.sms.DataClass.UserDTO;
import com.java.sms.exception.ApiException;
import com.java.sms.model.Role;
import com.java.sms.model.User;
import com.java.sms.repository.RoleRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.response.UserResponse;
import com.java.sms.security.JwtUtil;
import com.java.sms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;



/**
 * UserServiceImpl - Service implementation for managing User operations.
 *
 * This class handles user CRUD operations, image uploads, role mapping,
 * and password encryption. All business logic related to User goes here.
 */
@Service
public class UserServiceImpl implements UserService {


    private final JwtUtil jwtUtil;
    private  final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;





    @Value("${file.upload-dir}")
    private String UPLOAD_DIR; // src/main/resources/static/uploads/profileImages/


    /**
     * Constructor-based dependency injection.
     */
    @Autowired
    public UserServiceImpl(JwtUtil jwtUtil, UserRepository userRepository,
                           PasswordEncoder passwordEncoder, RoleRepository roleRepository) {

        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }




    /**
     * Fetch all users.
     *
     * @return List of UserResponse
     */
    @Override
    public List<UserResponse> getAllUser(){

        List<User> userList = userRepository.findAll();
        return userList.stream().map(UserResponse::new).toList();
    }


    /**
     * Get user by ID.
     *
     * @param id user ID
     * @return UserResponse object
     */
    public UserResponse getById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        return new UserResponse(user);


    }





    @Override
    public ResponseEntity<?> getUserImage(Long id) {

        // Step 1: Find user by ID from database.
        // If user not found, throw an exception.
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        // Step 2: Prepare a variable to hold the final full image URL.
        String imageUrl = null;

        // Step 3: Check if the user has an image path saved in DB.
        if (user.getImagePath() != null) {

            // Step 4: Get the base URL of your server
            // Example: http://localhost:8080
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

            // Step 5: Append the stored image path with the base URL
            // Example final result:
            // http://localhost:8080/uploads/profileImages/filename.png
            imageUrl = baseUrl + user.getImagePath();
        }

        // Step 6: Return response with user info + final image URL
        // This map returns as JSON like:
        // {
        //   "id": 1,
        //   "email": "abc@gmail.com",
        //   "imageUrl": "http://localhost:8080/uploads/profileImages/file.png"
        // }
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "imageUrl", imageUrl
        ));
    }



    /**
     * Upload only user image.
     *
     * @param userId user ID
     * @param image MultipartFile image
     * @return success response
     */

    @Override
    public ResponseEntity<?> uploadUserImage(Long userId, MultipartFile image) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image file is required"));
        }

        try {
            // Ensure directory exists (relative to project root)
            File uploadDirFile = new File(UPLOAD_DIR);
            if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

            // Clean filename (replace spaces)
            String originalName = image.getOriginalFilename() != null ? image.getOriginalFilename() : "file";
            originalName = originalName.replaceAll("\\s+", "_");

            // Unique filename
            String filename = UUID.randomUUID() + "_" + originalName;

            // Path build (use Paths.get to handle separators)
            Path destPath = Paths.get(UPLOAD_DIR, filename).toAbsolutePath();
            // Save file
            Files.copy(image.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);

            // LOG actual saved absolute path (for debugging)
            System.out.println("Saved image at: " + destPath.toString());
            System.out.println("Exists? " + Files.exists(destPath));

            // Save relative URL in DB
            String imageUrl = "/uploads/profileImages/" + filename;
            user.setImagePath(imageUrl);
            userRepository.save(user);

            // Return full URL to frontend
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            String fullImageUrl = baseUrl + imageUrl;

            return ResponseEntity.ok(Map.of(
                    "message", "Image uploaded successfully",
                    "userId", user.getId(),
                    "imageUrl", fullImageUrl
//                    "savedAbsolutePath", destPath.toString()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image", "details", e.getMessage()));
        }
    }



    @Override
    public ResponseEntity<?> updateUserImage(Long userId, MultipartFile newImage) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));

        if (newImage == null || newImage.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "New image is required"));
        }

        try {
            // Delete old image if exists
            if (user.getImagePath() != null) {
                // Extract filename, because DB stores: /uploads/profileImages/abc.png
                String oldFilename = user.getImagePath().replace("/uploads/profileImages/", "");

                File oldFile = new File(UPLOAD_DIR + oldFilename);
                if (oldFile.exists()) {
                    oldFile.delete();   // delete old image
                }
            }

            //  Create folder if not exists
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            //  Generate new filename
            String cleanName = newImage.getOriginalFilename().replaceAll("\\s+", "_");
            String filename = UUID.randomUUID() + "_" + cleanName;

            //  Save new image
            Path path = Paths.get(UPLOAD_DIR + filename);
            Files.copy(newImage.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // Save new URL in DB
            String imageUrl = "/uploads/profileImages/" + filename;
            user.setImagePath(imageUrl);

            userRepository.save(user);

            //  Full URL return
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            String fullUrl = baseUrl + imageUrl;

            return ResponseEntity.ok(Map.of(
                    "message", "Image updated successfully",
                    "imageUrl", fullUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update image", "details", e.getMessage()));
        }
    }


    /**
     * Update user with details.
     *
     * @param id user ID
     * @param request DTO containing update details
     */
    @Override
    public ResponseEntity<?> updateUser(Long id, UserDTO request) {
        try {
            // Find existing user
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

            //  Get role from Django service
//            RoleDTO role = roleClient.getRoleByName(request); // Feign client call

//             Update user details
            if (request.getFirstName()!=null) user.setFirstName(request.getFirstName());
            if (request.getMiddleName() !=null) user.setMiddleName(request.getMiddleName());
            if (request.getLastName() !=null) user.setLastName(request.getLastName());
            if (request.getEmail()!=null) user.setEmail(request.getEmail());
            if (request.getPassword()!=null) user.setPassword(passwordEncoder.encode(request.getPassword()));
            if (request.getGender() != null) user.setGender(request.getGender());
            if (request.getPhoneNo() != null) user.setPhoneNo(request.getPhoneNo());
            if (request.getAadharNo() != null) user.setAadharNo(request.getAadharNo());
            if (request.getPanNo() != null) user.setPanNo(request.getPanNo());
            if (request.getQualification() != null)user.setQualification(request.getQualification());


            if (request.getRoles()!= null && !request.getRoles().isEmpty()) {
                Set<RoleDTO> roleDTOs = request.getRoles();
                Set<Role> userRoles = new HashSet<>();
                for (RoleDTO roleDTO : roleDTOs) {
                    Role dbRole = roleRepository.findByName(roleDTO.getName())
                                .orElseThrow(() -> new RuntimeException("Role not found: " + roleDTO.getName()));
                    userRoles.add(dbRole);
                }
                user.setRoles(userRoles);
            }
//              Update image if provided
//            if (request.getImage() != null && !request.getImage().isEmpty()) {
//                File uploadDir = new File(UPLOAD_DIR);
//                if (!uploadDir.exists()) uploadDir.mkdirs();
//
//                // Generate unique filename
//                String filename = UUID.randomUUID() + "_" + request.getImage().getOriginalFilename();
//
//                // Save image inside resources/static/uploads/
//                Path path = Paths.get(UPLOAD_DIR + filename);
//                Files.copy(request.getImage().getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//                user.setImagePath(UPLOAD_DIR + filename);
//            }

            //  Save updated user
            userRepository.save(user);

            return ResponseEntity.ok("User updated successfully!");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating user: " + e.getMessage());
        }
    }


    /**
     * Delete user by ID.
     */
    @Override
    public ResponseEntity<String> deleteUser(Long id){
        User user = userRepository.findById(id).orElseThrow(
                () -> new ApiException("User Id not found",HttpStatus.NOT_FOUND));

        userRepository.delete(user);
        return ResponseEntity.ok("Deleted successfully");
    }









    /**
     * Create new user (without image).
     *
     * @param request user dto
     * @return newly created user
     */
    @Override
    public ResponseEntity<?> createUser(UserDTO request) {
        try {
            // 1. Check duplicate email
            if (userRepository.findByEmail(request.getEmail().toLowerCase()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }

            //  2. Create new user
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setMiddleName(request.getMiddleName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setAadharNo(request.getAadharNo());
            user.setGender(request.getGender());
            user.setPanNo(request.getPanNo());
            user.setPhoneNo(request.getPhoneNo());
            user.setQualification(request.getQualification());
            user.setActive(true);

            //  3. Set roles (if any)

            // Convert RoleDTO → Role Entity Set
            Set<Role> roleSet = request.getRoles().stream()
                    .map(roleDTO -> roleRepository.findByName(roleDTO.getName().toLowerCase())
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleDTO.getName())))
                    .collect(Collectors.toSet());

            user.setRoles(roleSet);


////              Update image if provided
//            if (request.getImage() != null && !request.getImage().isEmpty()) {
//                File uploadDir = new File(UPLOAD_DIR);
//                if (!uploadDir.exists()) uploadDir.mkdirs();
//
//                // Generate unique filename
//                String filename = UUID.randomUUID() + "_" + request.getImage().getOriginalFilename();
//
//                // Save image inside resources/static/uploads/
//                Path path = Paths.get(UPLOAD_DIR + filename);
//                Files.copy(request.getImage().getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//                user.setImagePath(UPLOAD_DIR + filename);
//            }

            //  5. Save user to DB
            User savedUser = userRepository.save(user);

            //  6. Prepare clean response (no password)
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", savedUser.getId());
            response.put("firstName", savedUser.getFirstName());
            response.put("middleName", savedUser.getMiddleName());
            response.put("lastName", savedUser.getLastName());
            response.put("email", savedUser.getEmail());
            response.put("phoneNo", savedUser.getPhoneNo());
            response.put("qualification", savedUser.getQualification());
            response.put("gender", savedUser.getGender());
            response.put("aadharNo", savedUser.getAadharNo());
            response.put("panNo", savedUser.getPanNo());
            response.put("active", savedUser.getActive());
            response.put("roles", savedUser.getRoles().stream().map(Role::getName).toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User creation failed", "details", e.getMessage()));
        }
    }



}
