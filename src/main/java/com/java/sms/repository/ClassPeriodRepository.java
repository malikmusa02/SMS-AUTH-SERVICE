package com.java.sms.repository;


import com.java.sms.model.ClassPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassPeriodRepository extends JpaRepository<ClassPeriod, Long> {
    // helper if ever needed locally
    List<ClassPeriod> findByNameIn(List<String> names);
}
