package com.java.sms.DataClass;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("middleName")
    private String middleName;
    @JsonProperty("lastName")
    private String lastName;
    
    private String email;
    
    private String password;
    
    @JsonProperty("phoneNo")
    private String phoneNo;
    private String qualification;
    private String gender;

    @JsonProperty("aadharNo")
    private String aadharNo;
    @JsonProperty("panNo")
    private String panNo;
    private MultipartFile image;
    private Set<RoleDTO> roles;
    private Boolean active = true;

}
