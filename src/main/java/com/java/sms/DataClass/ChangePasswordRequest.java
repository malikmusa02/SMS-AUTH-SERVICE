package com.java.sms.DataClass;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotNull
    private String currentPassword;
    @NotNull
    private String newPassword;
    @NotNull
    private String confirmPassword;

    // Getters and Setters
}

