package com.java.sms.serviceImpl;

import com.java.sms.DataClass.LoginRequest;
import com.java.sms.DataClass.ResetPasswordRequest;
import com.java.sms.DataClass.RoleEntities;
import com.java.sms.exception.ApiException;
import com.java.sms.model.PasswordResetToken;
import com.java.sms.model.Role;
import com.java.sms.model.User;
import com.java.sms.openFeignClient.*;
import com.java.sms.repository.PasswordResetTokenRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.response.RoleIdResponse;
import com.java.sms.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.java.sms.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.*;


/**
 * Service class handling authentication, JWT generation,
 * password change, and password reset operations.
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {


    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserRoleClient userRoleClient;
    private final RoleServiceForDjango roleServiceForDjango;


    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;

    /**
     * Constructs AuthServiceImpl with required dependencies.
     */
    @Autowired
    public AuthServiceImpl(JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserRoleClient userRoleClient,
                           UserRepository userRepository, RoleServiceForDjango roleServiceForDjango, EmailService emailService1,
                           PasswordResetTokenRepository tokenRepository) {

        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userRoleClient = userRoleClient;
        this.roleServiceForDjango = roleServiceForDjango;
        this.emailService = emailService1;
        this.tokenRepository = tokenRepository;
    }



    // Helper method
    /** Safely concatenates first, middle, and last name */
    public static String getFullName(User user) {
        return String.join(" ",
                user.getFirstName() != null ? user.getFirstName() : "",
                user.getMiddleName() != null ? user.getMiddleName() : "",
                user.getLastName() != null ? user.getLastName() : ""
        ).trim();
    }




    /**
     * Authenticates a user and generates access/refresh tokens.
     *
     * <p>This method also fetches the user’s role-specific IDs
     * (StudentId, TeacherId, GuardianId, etc.)
     * from the Django microservice.
     *
     * <p>The result contains:
     * <ul>
     *     <li>JWT Access Token</li>
     *     <li>JWT Refresh Token</li>
     *     <li>User basic info</li>
     *     <li>List of assigned roles</li>
     *     <li>roleEntities: Map of ROLE → RoleTableId</li>
     * </ul>
     */
    public Map<String, Object> login(LoginRequest request) {

        // 1. Fetch user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found!", HttpStatus.NOT_FOUND));

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid password!", HttpStatus.CONFLICT);
        }

        // 3. Extract roles
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        // 4. Generate JWT tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), roleNames);
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), roleNames);

        // 5. Prepare response map
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "User logged in successfully");
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("userId", user.getId());
        response.put("roles", roleNames);
        response.put("name", getFullName(user));
        response.put("userProfile", user.getImagePath());

        // 6. Fetch Django role-specific IDs
        RoleEntities roleEntities = roleServiceForDjango.fetchRoleEntitiesForUser(user.getId());

        // 7. Add role mapping to response
        response.put("roleEntities", roleEntities.toMap());
        response.put("roleFetchStatus", roleEntities.isEmpty() ? "UNAVAILABLE" : "OK");

        return response;
    }






    /**
     * Generates a new access+refresh token pair using a valid refresh token.
     *
     * @param request request containing refreshToken
     * @return updated token pair and user info
     */
    @Override
    public ResponseEntity<?> generateRefreshToAccessToken(Map<String, String> request) {


        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token is missing");
        }

        //  Validate the refresh token
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));


        // Convert Set<Role> → List<Role>
        List<Role> roleList = new ArrayList<>(user.getRoles());

        // Extract role names
        List<String> roleNames = roleList.stream()
                .map(Role::getName)
                .toList();

        //  Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), roleNames);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), roleNames);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("userId", user.getId());
        response.put("userName",getFullName(user));
        response.put("roles", roleNames);
        response.put("userProfile", user.getImagePath());

        RoleEntities roleEntities = roleServiceForDjango.fetchRoleEntitiesForUser(user.getId());

        //  Add role mapping to response
        response.put("roleEntities", roleEntities.toMap());
        response.put("roleFetchStatus", roleEntities.isEmpty() ? "UNAVAILABLE" : "OK");


        return ResponseEntity.ok(response);

    }





    /**
     * Changes user password by validating current password and updating to new one.
     */
    @Transactional
    public String changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        // Step 1: Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        // Step 2: Validate current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ApiException("Current password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        // Step 3: Check if new and confirm match
        if (!newPassword.equals(confirmPassword)) {
            throw new ApiException("New password and confirm password do not match", HttpStatus.BAD_REQUEST);
        }

        // Step 4: Check if new password same as old
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ApiException("New password must be different from the old password", HttpStatus.BAD_REQUEST);
        }

        // Step 5: Encode and update
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.saveAndFlush(user);

        return "Password changed successfully";
    }





    /**
     * Sends a password reset link to the user's email and stores a reset token.
     *
     * @param email user email
     */
    @Transactional
    public void sendResetLink(String email) {
        // Step 1: Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found with email : " + email , HttpStatus.NOT_FOUND));

        // Step 2: Delete any previous reset tokens for this user (to avoid multiple active tokens)
        tokenRepository.deleteByUser(user);

        // Step 3: Generate a new secure token
        String token = UUID.randomUUID().toString();

        // Step 4: Create and save PasswordResetToken entity
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(60)); // valid for 1 hour
        tokenRepository.save(resetToken);

        // Step 5: Build the password reset URL (frontend link)
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        //  If you have a frontend running on port 3000 (React/Angular), send token in query param.

        // Step 6: Send email to the user
        emailService.sendEmail(
                user.getEmail(),
                "Password Reset Request",
                "Hello " + user.getFirstName() + " " + user.getMiddleName() + ",\n\n" +
                        "Click the link below to reset your password:\n" + resetUrl + "\n\n" +
                        "This link will expire in 60 minutes."
        );
    }




    /**
     * Resets user password by validating the reset token and updating the password.
     *
     * @param request reset token + new password details
     * @return success message
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        // Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApiException("Invalid token", HttpStatus.BAD_REQUEST));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token expired", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);


        tokenRepository.delete(resetToken);
        return "Password reset successful";
    }


}
