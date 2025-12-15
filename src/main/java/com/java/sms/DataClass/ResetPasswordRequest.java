package com.java.sms.DataClass;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotNull
    private String token;
    @NotNull
    private String newPassword;
    @NotNull
    private String confirmPassword;


}

