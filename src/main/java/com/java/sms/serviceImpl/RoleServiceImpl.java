package com.java.sms.serviceImpl;


import com.java.sms.DataClass.RoleDTO;
import com.java.sms.exception.ApiException;
import com.java.sms.model.Role;
import com.java.sms.repository.RoleRepository;
import com.java.sms.response.RoleResponse;
import com.java.sms.service.RoleService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Service implementation for managing system roles.
 *
 * <p>This service is responsible for:
 * <ul>
 *     <li>Creating new roles</li>
 *     <li>Fetching roles by ID</li>
 *     <li>Retrieving all roles with sorting</li>
 *     <li>Updating an existing role</li>
 *     <li>Deleting roles safely</li>
 * </ul>
 *
 * <p>All failures throw {@link ApiException}, which is handled
 * globally by the exception handler.
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }



    /**
     * Creates a new role if the role does not already exist.
     *
     * @param role Incoming role data (DTO)
     * @return RoleResponse containing saved role details
     * @throws ApiException if role with same name already exists
     */
    @Override
    public RoleResponse createRole(RoleDTO role) {
        if (roleRepository.existsByName(role.getName().toLowerCase())) {
            throw new ApiException("Role already exists: " + role.getName(), HttpStatus.CONFLICT);
        }
        Role role1 = new Role();
        role1.setName(role.getName().toLowerCase());
        Role save = roleRepository.save(role1);
        return new RoleResponse(save);
    }




    /**
     * Fetches a role by its unique ID.
     *
     * @param id Role ID
     * @return RoleResponse containing role details
     * @throws ApiException if no role exists with given ID
     */
    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Role not found with id: " + id , HttpStatus.NOT_FOUND));

        return new RoleResponse(role);
    }



    /**
     * Returns all roles sorted by role ID (ascending).
     *
     * @return List of RoleResponse objects
     */
    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> all = roleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return all.stream().map(RoleResponse::new).toList();
    }




    /**
     * Updates an existing role.
     *
     * @param id Role ID to update
     * @param updatedRole DTO containing updated name
     * @return RoleResponse containing updated role details
     * @throws ApiException if role not found or name already taken
     */
    @Override
    public RoleResponse updateRole(Long id, RoleDTO updatedRole) {

        Role role = roleRepository.findById(id).orElseThrow(
                () -> new ApiException("Role id : " + id  + " not found", HttpStatus.NOT_FOUND));


        // Ensure new role name does not conflict with an existing role
        boolean nameExists = roleRepository.existsByName(updatedRole.getName().toLowerCase());

        // Allow same name only if it is the same record
        if (nameExists && !role.getName().equalsIgnoreCase(updatedRole.getName())) {
            throw new ApiException("Role already exists: " + updatedRole.getName(), HttpStatus.CONFLICT);
        }

        role.setName(updatedRole.getName().toLowerCase());
        Role save = roleRepository.save(role);
        return new RoleResponse(save);

    }




    /**
     * Deletes a role by ID.
     *
     * @param id Role ID
     * @throws ApiException if no role exists with provided ID
     */
    @Override
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(
                () -> new ApiException("Role id : " + id  + " not found", HttpStatus.NOT_FOUND));
        roleRepository.delete(role);
    }

}
