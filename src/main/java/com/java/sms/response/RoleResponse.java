package com.java.sms.response;


import com.java.sms.model.Role;
import lombok.Data;

@Data
public class RoleResponse {

    private Long id;
    private String name;

    public RoleResponse(Role request) {

        this.id = request.getId();
        this.name = request.getName();
    }
}
