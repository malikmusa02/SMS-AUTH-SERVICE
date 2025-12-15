package com.java.sms.service;


import com.java.sms.model.ClassRoom;
import java.util.List;

public interface ClassRoomService {

    List<ClassRoom> getAll();

    ClassRoom getById(Long id);

    ClassRoom create(ClassRoom classRoom);

    ClassRoom update(Long id, ClassRoom classRoom);

    void delete(Long id);

}

