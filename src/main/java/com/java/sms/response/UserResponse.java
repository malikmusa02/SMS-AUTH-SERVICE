package com.java.sms.response;

import com.java.sms.model.Role;
import com.java.sms.model.User;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserResponse {

    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String imageUrl;       // Full URL for frontend
    private String phoneNo;
    private String qualification;
    private String gender;
    private String aadharNo;
    private String panNo;
    private Boolean active;
    private Set<String> roles;     // Simplified: show only role names

    // Constructor with baseUrl (for frontend image display)
    public UserResponse(User request, String baseUrl) {
        this.id = request.getId();
        this.firstName = request.getFirstName();
        this.middleName = request.getMiddleName();
        this.lastName = request.getLastName();
        this.email = request.getEmail();
        this.phoneNo = request.getPhoneNo();
        this.qualification = request.getQualification();
        this.gender = request.getGender();
        this.aadharNo = request.getAadharNo();
        this.panNo = request.getPanNo();
        this.active = request.getActive();

        // Build full image URL for frontend (only if imagePath exists)
        if (request.getImagePath() != null && !request.getImagePath().isEmpty()) {
            this.imageUrl = baseUrl + request.getImagePath(); // e.g., http://localhost:8080/uploads/profile.jpg
        } else {
            this.imageUrl = null;
        }

        // Convert Role entities to role name list
        if (request.getRoles() != null) {
            this.roles = request.getRoles()
                    .stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
        }
    }

    // Constructor without baseUrl (if not needed)
    public UserResponse(User request) {
        this(request, "");
    }
}
