package com.java.sms.DataClass;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginRequest {

    @NotNull
    private String email;
    @NotNull
    private String password;

}
