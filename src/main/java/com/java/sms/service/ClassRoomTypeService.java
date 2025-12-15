package com.java.sms.service;


import com.java.sms.model.ClassRoomType;
import java.util.List;

public interface ClassRoomTypeService {

    List<ClassRoomType> getAll();

    ClassRoomType getById(Long id);

    ClassRoomType create(ClassRoomType classRoomType);

    ClassRoomType update(Long id, ClassRoomType classRoomType);

    void delete(Long id);

}
