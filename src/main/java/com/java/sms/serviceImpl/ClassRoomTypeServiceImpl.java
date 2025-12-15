package com.java.sms.serviceImpl;


import com.java.sms.exception.ApiException;
import com.java.sms.model.ClassRoomType;
import com.java.sms.repository.ClassRoomTypeRepository;
import com.java.sms.service.ClassRoomTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for managing Classroom Types.
 * Provides CRUD operations with validation and exception handling.
 */
@Service
public class ClassRoomTypeServiceImpl implements ClassRoomTypeService {

    private final ClassRoomTypeRepository repository;


    /**
     * Constructor for injecting repository dependency.
     *
     * @param repository ClassRoomTypeRepository instance
     */
    public ClassRoomTypeServiceImpl(ClassRoomTypeRepository repository) {
        this.repository = repository;
    }


    /**
     * Fetch all classroom types.
     *
     * @return List of ClassRoomType
     */
    @Override
    public List<ClassRoomType> getAll() {
        return repository.findAll();
    }


    /**
     * Fetch a classroom type by its ID.
     *
     * @param id Classroom type ID
     * @return ClassRoomType entity
     * @throws ApiException if not found
     */
    @Override
    public ClassRoomType getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException("Data not found", HttpStatus.NOT_FOUND));
    }



    /**
     * Create a new classroom type.
     * Converts name to lowercase and checks for duplicate records.
     *
     * @param type ClassRoomType object
     * @return saved ClassRoomType
     * @throws ApiException if type already exists
     */
    @Override
    public ClassRoomType create(ClassRoomType type) {
        type.setName(type.getName().toLowerCase());
        if (repository.existsByNameIgnoreCase(type.getName())) {
            throw new ApiException("Classroom Type Already Exist",HttpStatus.CONFLICT);
        }
        return repository.save(type);
    }


    /**
     * Update an existing classroom type.
     * Validates name uniqueness before update.
     *
     * @param id   Classroom type ID
     * @param type Updated data
     * @return updated ClassRoomType
     * @throws ApiException if duplicate or not found
     */
    @Override
    public ClassRoomType update(Long id, ClassRoomType type) {
        ClassRoomType existing = getById(id);
        type.setName(type.getName().toLowerCase());
        if (repository.existsByNameIgnoreCase(type.getName()) && !existing.getId().equals(id)) {
            throw new ApiException("Classroom Type Already Exist", HttpStatus.CONFLICT);
        }
        existing.setName(type.getName());
        return repository.save(existing);
    }


    /**
     * Delete a classroom type by ID.
     *
     * @param id Classroom type ID
     */
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
