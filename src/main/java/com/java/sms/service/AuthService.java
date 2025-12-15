package com.java.sms.service;

import com.java.sms.DataClass.LoginRequest;
import com.java.sms.DataClass.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;



public interface AuthService {

    Map<String, Object> login(LoginRequest request);

    String changePassword(Long userId, String currentPassword, String newPassword, String confirmPassword);

    void sendResetLink(String email);

    String resetPassword(ResetPasswordRequest request);

    ResponseEntity<?> generateRefreshToAccessToken(Map<String, String> request);
}
