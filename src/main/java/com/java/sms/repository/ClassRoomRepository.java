package com.java.sms.repository;


import com.java.sms.model.ClassRoom;
import com.java.sms.model.ClassRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

    boolean existsByRoomTypeAndRoomNameIgnoreCase(ClassRoomType roomType, String roomName);

}

