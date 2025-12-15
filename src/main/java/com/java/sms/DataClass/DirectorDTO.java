package com.java.sms.DataClass;


import com.java.sms.model.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DirectorDTO {
    private Long id;
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
    private MultipartFile imagePath;
    private String phoneNo;
    private String qualification;
    private String gender;
    private String aadharNo;
    private String panNo;
    private Boolean active = true;
}

