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
import com.fasterxml.jackson.annotation.JsonProperty;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GuardianDTO {
     @JsonProperty("user")
    private Long user;       // user_id in Django

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String password;
    private MultipartFile image;

    @JsonProperty("panNo")
    private String phoneNo;
    private String qualification;
    private String gender;

    @JsonProperty("aadharNo")
    private String aadharNo;

    @JsonProperty("panNo")
    private String panNo;
    
    private Set<RoleDTO> roles;
    private Boolean active = true;
}
