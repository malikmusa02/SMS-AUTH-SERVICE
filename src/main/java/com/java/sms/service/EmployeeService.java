package com.java.sms.service;


import com.java.sms.DataClass.EmployeeRequest;
import com.java.sms.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    Object listEmployees(String role, Long id, String name);

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse updateEmployee(EmployeeRequest request);

    void deleteEmployee(Long id);
}
