package com.java.sms.DataClass;

import com.java.sms.model.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

//@NoArgsConstructor
//@AllArgsConstructor
@Data
public class OfficeStaffDTO {
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
//    private MultipartFile image;
    private String phoneNo;
    private String qualification;
    private String gender;
    private String aadharNo;
    private Set<RoleDTO> roles;
    private String panNo;
    private Boolean active = true;
}

