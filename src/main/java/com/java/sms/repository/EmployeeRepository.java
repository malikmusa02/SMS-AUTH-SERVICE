package com.java.sms.repository;


import com.java.sms.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByUser_Id(Long userId);

    Optional<Employee> findByUser_Id(Long userId);
}
