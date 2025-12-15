package com.java.sms.serviceImpl;


import com.java.sms.DataClass.EmployeeRequest;
import com.java.sms.DataClass.UserBasicResponse;
import com.java.sms.exception.ApiException;
import com.java.sms.model.Employee;
import com.java.sms.model.User;
import com.java.sms.repository.EmployeeRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.response.EmployeeResponse;
import com.java.sms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Employee service implementation.
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepo;
    private final UserRepository userRepository;




    @Override
    public Object listEmployees(String role, Long id, String name) {

        /* === GET BY ID === */
        if (id != null) {
            Employee emp = employeeRepo.findById(id)
                    .orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));
            return toResponse(emp);
        }


        /* === ROLE FILTER (teacher / office staff not yet employees) === */
        if (role != null) {

            List<User> users = userRepository.findUsersByRoles(role);

            List<Long> employeeUserIds =
                    employeeRepo.findAll().stream()
                            .map(e -> e.getUser().getId())
                            .toList();



            return users.stream()
                    .filter(u -> !employeeUserIds.contains(u.getId()))
                    .map(this::toBasicUserResponse)
                    .toList();
        }


        /* === NAME SEARCH === */
        return employeeRepo.findAll().stream()
                .filter(e ->
                        name == null ||
                                e.getUser().getFirstName().toLowerCase().contains(name.toLowerCase()) ||
                                e.getUser().getMiddleName().toLowerCase().contains(name.toLowerCase()) ||
                                e.getUser().getLastName().toLowerCase().contains(name.toLowerCase())
                )
                .map(this::toResponse)
                .distinct()
                .collect(Collectors.toList());
    }








    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        User user = userRepository.findById(request.getUser())
                .orElseThrow(() -> new ApiException("Invalid user id", HttpStatus.BAD_REQUEST));

        List<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getName().toLowerCase())
                .toList();

        if (!roles.contains("teacher") && !roles.contains("office staff")) {
            throw new ApiException(
                    "Only Teacher or Office Staff can be assigned as employee",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (employeeRepo.existsByUser_Id(user.getId())) {
            throw new ApiException(
                    "Salary is already created for " + user.getFirstName() + " " + user.getLastName(),
                    HttpStatus.BAD_REQUEST
            );
        }

        Employee emp = Employee.builder()
                .user(user)
                .baseSalary(request.getBaseSalary())
                .build();

        return toResponse(employeeRepo.save(emp));
    }




    @Override
    public EmployeeResponse updateEmployee(EmployeeRequest request) {

        Employee emp = employeeRepo.findByUser_Id(request.getUser())
                .orElseThrow(() ->
                        new ApiException("Employee not found with this user", HttpStatus.NOT_FOUND)
                );

        emp.setBaseSalary(request.getBaseSalary());

        return toResponse(employeeRepo.save(emp));
    }









    @Override
    public void deleteEmployee(Long id) {

        Employee emp = employeeRepo.findById(id)
                .orElseThrow(() -> new ApiException("Employee not found", HttpStatus.NOT_FOUND));

        employeeRepo.delete(emp);
    }









    /* ===== Helper Mapper ===== */
    private EmployeeResponse toResponse(Employee emp) {
        return EmployeeResponse.builder()
                .id(emp.getId())
                .user(emp.getUser().getId())
                .name(emp.getUser().getFirstName())

                .role(emp.getUser().getRoles().stream()
                        .map(r -> r.getName())
                        .toList())

                .baseSalary(emp.getBaseSalary())
                .build();
    }


    private UserBasicResponse toBasicUserResponse(User user) {
        return UserBasicResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }





}
