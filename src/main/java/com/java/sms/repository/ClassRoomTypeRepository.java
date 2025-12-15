package com.java.sms.repository;


import com.java.sms.model.ClassRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassRoomTypeRepository extends JpaRepository<ClassRoomType, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<ClassRoomType> findByNameIgnoreCase(String name);

}
