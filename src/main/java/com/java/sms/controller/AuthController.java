package com.java.sms.controller;


import com.java.sms.DataClass.ChangePasswordRequest;
import com.java.sms.DataClass.LoginRequest;

import com.java.sms.DataClass.ResetPasswordRequest;
import com.java.sms.security.JwtBlacklistService;
import com.java.sms.security.JwtUtil;
import com.java.sms.service.AuthService;
import feign.Headers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class AuthController {


    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;
    private final AuthService authService;

    public AuthController( JwtUtil jwtUtil, JwtBlacklistService jwtBlacklistService, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.jwtBlacklistService = jwtBlacklistService;
        this.authService = authService;
    }


//    @Headers("Content-Type: application/json")
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        return authService.generateRefreshToAccessToken(request);
    }




        @PutMapping("/changePassword/{id}")
        public ResponseEntity<String> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {

         String message = authService.changePassword(
                id,
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getConfirmPassword());

            return ResponseEntity.ok(message);
    }


//    @PostMapping("/logout")
//    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//
//            long expiry = jwtUtil.getExpiry(token); // get expiry
//            jwtBlacklistService.blacklistToken(token, expiry);
//        }
//
//        return ResponseEntity.ok("User logged out successfully!");
//    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody String authHeader) {
        if (authHeader != null && authHeader.startsWith("Refresh Token ")) {
            String token = authHeader.substring(14);

            long expiry = jwtUtil.getExpiry(token); // get expiry
            jwtBlacklistService.blacklistToken(token, expiry);
        }

        return ResponseEntity.ok("User logged out successfully!");
    }


    // Step 1: Forgot password
    @PostMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.sendResetLink(email);
        return ResponseEntity.ok("Password reset link sent to your email");
    }


    // Step 2: Reset password
    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {

        String msg = authService.resetPassword(request);
        return ResponseEntity.ok(msg);
    }




}

