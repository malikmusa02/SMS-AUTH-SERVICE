package com.java.sms.controller;


import com.java.sms.DataClass.AssignRequest;
import com.java.sms.DataClass.ClassPeriodRequest;
import com.java.sms.model.ClassPeriod;
import com.java.sms.service.ClassPeriodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for ClassPeriod CRUD and the assign-to-yearlevel forwarding endpoint.
 *
 * Endpoints:
 *  - GET  /api/class-periods           -> list all
 *  - GET  /api/class-periods/{id}      -> get by id
 *  - POST /api/class-periods           -> create (validates Django foreign keys)
 *  - PUT  /api/class-periods/{id}      -> update (partial updates allowed)
 *  - DELETE /api/class-periods/{id}    -> delete
 *  - POST /api/class-periods/assign-to-yearlevel -> forward assign request to Django via Feign
 *
 * All exceptions are handled by GlobalExceptionHandler which translates ResponseStatusException to HTTP responses.
 */
@RestController
@RequestMapping("/class-periods")
public class ClassPeriodController {

    private final ClassPeriodService service;

    public ClassPeriodController(ClassPeriodService service) {
        this.service = service;
    }





    /**
     * Get all ClassPeriods.
     *
     * @return 200 OK with list of ClassPeriod (may be empty)
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<ClassPeriod>> getAll() {
        List<ClassPeriod> list = service.getAllClassPeriods();
        return ResponseEntity.ok(list);
    }





    /**
     * Get a single ClassPeriod by id.
     *
     * @param id the id of the ClassPeriod
     * @return 200 OK with the ClassPeriod; 404 if not found (handled by service)
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<ClassPeriod> getById(@PathVariable("id") Long id) {
        ClassPeriod cp = service.getClassPeriodById(id);
        return ResponseEntity.ok(cp);
    }





    /**
     * Create a new ClassPeriod.
     *
     * <p>Validates foreign keys by calling Django lookup endpoints; if valid, saves locally.</p>
     *
     * @param req payload for ClassPeriod creation
     * @return 201 CREATED with saved entity
     */
    @PostMapping("/create")
    public ResponseEntity<ClassPeriod> create(@Valid @RequestBody ClassPeriodRequest req) {
        ClassPeriod saved = service.createClassPeriod(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }






    /**
     * Update an existing ClassPeriod (partial updates allowed).
     *
     * <p>If the request contains any of the Django-backed foreign keys (subjectId, yearLevelId,
     * teacherId, termId) they will be validated against Django before saving.</p>
     *
     * @param id  id of the ClassPeriod to update
     * @param req request payload containing fields to update (nulls are ignored)
     * @return 200 OK with updated entity
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ClassPeriod> update(@PathVariable("id") Long id,
                                              @Valid @RequestBody ClassPeriodRequest req) {
        ClassPeriod updated = service.updateClassPeriod(id, req);
        return ResponseEntity.ok(updated);
    }






    /**
     * Delete a ClassPeriod by id.
     *
     * @param id the id to delete
     * @return 204 NO CONTENT on success
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.deleteClassPeriod(id);
        return ResponseEntity.noContent().build();
    }






//    /**
//     * Forward assign-to-yearlevel request to Django.
//     *
//     * Example body:
//     * {
//     *   "yearLevelName": "Grade 9",
//     *   "classPeriodNames": ["Math A", "Science B"]
//     * }
//     *
//     * @param req assign request DTO
//     * @return 200 OK with a wrapper message and Django's returned details
//     */
//    @PostMapping("/assign-to-yearlevel")
//    public ResponseEntity<Map<String, Object>> assignToYearLevel(@Valid @RequestBody AssignRequest req) {
//        Map<String, Object> djangoResp = service.assignToYearLevel(req);
//        Map<String, Object> response = Map.of(
//                "message", "ClassPeriods assigned successfully.",
//                "details", djangoResp
//        );
//        return ResponseEntity.ok(response);
//    }
//



}
