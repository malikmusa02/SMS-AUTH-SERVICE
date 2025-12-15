package com.java.sms.service;



import com.java.sms.DataClass.RoleDTO;
import com.java.sms.model.Role;
import com.java.sms.response.RoleResponse;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(RoleDTO role);
    RoleResponse getRoleById(Long id);
    List<RoleResponse> getAllRoles();
    RoleResponse updateRole(Long id, RoleDTO updatedRole);
    void deleteRole(Long id);

}

