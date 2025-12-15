package com.java.sms.serviceImpl;



import com.java.sms.exception.ApiException;
import com.java.sms.model.ClassRoom;
import com.java.sms.model.ClassRoomType;
import com.java.sms.repository.ClassRoomRepository;
import com.java.sms.repository.ClassRoomTypeRepository;
import com.java.sms.service.ClassRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for managing ClassRoom entities.
 * Handles CRUD operations with validation for room type and room uniqueness.
 */
@Service
public class ClassRoomServiceImpl implements ClassRoomService {

    private final ClassRoomRepository repository;
    private final ClassRoomTypeRepository typeRepository;


    /**
     * Constructor for injecting repository dependencies.
     *
     * @param repository      ClassRoomRepository instance
     * @param typeRepository  ClassRoomTypeRepository instance
     */
    public ClassRoomServiceImpl(ClassRoomRepository repository, ClassRoomTypeRepository typeRepository) {
        this.repository = repository;
        this.typeRepository = typeRepository;
    }


    /**
     * Retrieve all classrooms.
     *
     * @return List of ClassRoom
     */
    @Override
    public List<ClassRoom> getAll() {
        return repository.findAll();
    }


    /**
     * Fetch a classroom by ID.
     *
     * @param id Classroom ID
     * @return ClassRoom entity
     * @throws ApiException if not found
     */
    @Override
    public ClassRoom getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException("Data not found", HttpStatus.NOT_FOUND));
    }


    /**
     * Create a new classroom.
     * Validates room type and ensures the same room does not exist under the same type.
     *
     * @param classRoom ClassRoom object
     * @return saved ClassRoom entity
     * @throws ApiException if room type invalid or room already exists
     */
    @Override
    public ClassRoom create(ClassRoom classRoom) {
        ClassRoomType type = typeRepository.findById(classRoom.getRoomType().getId())
                .orElseThrow(() -> new ApiException("Invalid Room Type", HttpStatus.BAD_REQUEST));

        if (repository.existsByRoomTypeAndRoomNameIgnoreCase(type, classRoom.getRoomName())) {
            throw new ApiException("This room already exists for this type",HttpStatus.CONFLICT);
        }

        classRoom.setRoomType(type);
        return repository.save(classRoom);
    }



    /**
     * Update an existing classroom.
     * Ensures unique room name within the given room type.
     *
     * @param id        Classroom ID
     * @param classRoom Updated data
     * @return updated ClassRoom
     * @throws ApiException if room type invalid or duplicate room exists
     */
    @Override
    public ClassRoom update(Long id, ClassRoom classRoom) {
        ClassRoom existing = getById(id);
        ClassRoomType type = typeRepository.findById(classRoom.getRoomType().getId())
                .orElseThrow(() -> new ApiException("Invalid Room Type", HttpStatus.BAD_REQUEST));

        if (repository.existsByRoomTypeAndRoomNameIgnoreCase(type, classRoom.getRoomName())
                && !existing.getId().equals(id)) {
            throw new ApiException("This room already exists for this type", HttpStatus.CONFLICT);
        }

        existing.setRoomType(type);
        existing.setRoomName(classRoom.getRoomName());
        existing.setCapacity(classRoom.getCapacity());
        return repository.save(existing);
    }



    /**
     * Delete classroom by ID.
     *
     * @param id Classroom ID
     */
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}

