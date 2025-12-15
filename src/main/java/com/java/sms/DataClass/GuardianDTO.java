package com.java.sms.DataClass;


import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GuardianDTO {
    private Long user;       // user_id in Django
    private String firstName;
    private String middleName;
    private String lastName;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String password;
    private MultipartFile image;
    private String phoneNo;
    private String qualification;
    private String gender;
    private String aadharNo;
    private String panNo;
    private Set<RoleDTO> roles;
    private Boolean active = true;
}
