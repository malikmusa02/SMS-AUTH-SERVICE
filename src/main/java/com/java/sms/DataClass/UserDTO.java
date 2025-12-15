package com.java.sms.DataClass;



import lombok.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNo;
    private String qualification;
    private String gender;
    private String aadharNo;
    private String panNo;
    private MultipartFile image;
    private Set<RoleDTO> roles;
    private Boolean active = true;

}
