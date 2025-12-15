package com.java.sms.serviceImpl;


import com.java.sms.DataClass.AssignRequest;
import com.java.sms.DataClass.ClassPeriodRequest;
import com.java.sms.model.ClassPeriod;
import com.java.sms.openFeignClient.DjangoLookupClient;
import com.java.sms.repository.ClassPeriodRepository;
import com.java.sms.service.ClassPeriodService;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;


/**
 * Service layer for ClassPeriod operations.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>CRUD operations on local ClassPeriod entities</li>
 *     <li>Validation of foreign keys by calling Django lookup endpoints (via Feign)</li>
 *     <li>Forwarding "assign-to-yearlevel" requests to Django</li>
 * </ul>
 * </p>
 *
 * <p>Errors are translated to {@link org.springframework.web.server.ResponseStatusException}
 * so controllers can return appropriate HTTP responses.</p>
 */
@Service
public class ClassPeriodServiceImpl implements ClassPeriodService {

    private final ClassPeriodRepository classPeriodRepository;

    private final DjangoLookupClient lookupClient;

    public ClassPeriodServiceImpl(ClassPeriodRepository classPeriodRepository,

                                  DjangoLookupClient lookupClient) {
        this.classPeriodRepository = classPeriodRepository;
        this.lookupClient = lookupClient;
    }






    /**
     * Validate each Django-backed FK by calling lookup endpoints and inspecting the returned JSON map.
     * Throws ResponseStatusException(400) with a clear list of invalid IDs when any check fails.
     */
    private void validateForeignKeys(ClassPeriodRequest req) {
        // Guard missing IDs first
        if (req.getSubjectId() == null || req.getYearLevelId() == null ||
                req.getTeacherId() == null || req.getTermId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Missing required FK(s): subjectId, yearLevelId, teacherId, termId.");
        }

        StringBuilder problems = new StringBuilder();

        // Helper to call a lookup and validate returned map
        BiConsumer<String, Long> check = (name, id) -> {
            try {
                Map<String, Object> resp;
                switch (name) {
                    case "subject":
                        resp = lookupClient.getSubjectById(id);
                        break;
                    case "yearlevel":
                        resp = lookupClient.getYearLevelById(id);
                        break;
                    case "teacher":
                        resp = lookupClient.getTeacherById(id);
                        break;
                    case "term":
                        resp = lookupClient.getTermById(id);
                        break;
                    default:
                        resp = null;
                }

                // Validate response map
                if (resp == null || resp.isEmpty()) {
                    problems.append(String.format("%sId=%d (empty response); ", name, id));
                    return;
                }

                // check id field exists and matches requested id
                Object returnedId = resp.get("id");
                if (returnedId == null) {
                    problems.append(String.format("%sId=%d (no 'id' in response); ", name, id));
                    return;
                }
                // compare numeric values safely
                long rid;
                try {
                    rid = ((Number) returnedId).longValue();
                } catch (ClassCastException ex) {
                    // sometimes serializers return string — try parse
                    try { rid = Long.parseLong(returnedId.toString()); }
                    catch (Exception e) { problems.append(String.format("%sId=%d (invalid 'id' type); ", name, id)); return; }
                }
                if (rid != id) {
                    problems.append(String.format("%sId=%d (response id=%d mismatch); ", name, id, rid));
                    return;
                }

                // optional: if response contains is_active and it's false, treat as invalid
                if (resp.containsKey("is_active")) {
                    Object active = resp.get("is_active");
                    if (active instanceof Boolean && !((Boolean) active)) {
                        problems.append(String.format("%sId=%d (not active); ", name, id));
                    }
                }

            } catch (feign.FeignException.NotFound nf) {
                problems.append(String.format("%sId=%d (NOT FOUND); ", name, id));
            } catch (feign.FeignException fe) {
                // include truncated body/message to help debugging
                problems.append(String.format("%sId=%d (error: %s); ", name, id, getFeignBodySafe(fe)));
            } catch (Exception ex) {
                problems.append(String.format("%sId=%d (unexpected: %s); ", name, id, ex.getMessage()));
            }
        };

        // Run checks
        check.accept("subject", req.getSubjectId());
        check.accept("yearlevel", req.getYearLevelId());
        check.accept("teacher", req.getTeacherId());
        check.accept("term", req.getTermId());

        if (problems.length() > 0) {
            String msg = "One or more foreign keys invalid or lookup failed: " + problems.toString().trim();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }
    }





    /** Extract feign body safely and truncate to keep messages small */
    private String getFeignBodySafe(feign.FeignException e) {
        try {
            String content = e.contentUTF8();
            if (content == null) return e.getMessage();
            if (content.length() > 800) return content.substring(0, 800) + "...(truncated)";
            return content;
        } catch (Exception ex) {
            return e.getMessage();
        }
    }







    /**
     * Create and persist a new ClassPeriod after validating Django foreign keys.
     *
     * @param req DTO containing class period data; must include subjectId, yearLevelId, teacherId and termId
     * @return the persisted {@link ClassPeriod}
     * @throws ResponseStatusException 400 if any foreign key is invalid
     * @throws ResponseStatusException 502 if Django service is unreachable
     */
    @Transactional
    public ClassPeriod createClassPeriod(ClassPeriodRequest req) {
        validateForeignKeys(req);

        ClassPeriod cp = new ClassPeriod();
        cp.setSubjectId(req.getSubjectId());
        cp.setYearLevelId(req.getYearLevelId());
        cp.setTeacherId(req.getTeacherId());
        cp.setTermId(req.getTermId());
        cp.setStartTimeId(req.getStartTimeId());
        cp.setEndTimeId(req.getEndTimeId());
        cp.setClassroomId(req.getClassroomId());
        cp.setName(req.getName());
        return classPeriodRepository.save(cp);
    }





    /**
     * Retrieve a ClassPeriod by its ID.
     *
     * @param id the ID of the ClassPeriod to fetch
     * @return the found {@link ClassPeriod}
     * @throws ResponseStatusException 404 if no ClassPeriod exists with the given id
     */
    @Transactional(readOnly = true)
    public ClassPeriod getClassPeriodById(Long id) {
        Optional<ClassPeriod> opt = classPeriodRepository.findById(id);
        return opt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "ClassPeriod not found with id: " + id));
    }




    /**
     * Retrieve all ClassPeriods.
     *
     * @return list of {@link ClassPeriod}; never null (may be empty)
     */
    @Transactional(readOnly = true)
    public List<ClassPeriod> getAllClassPeriods() {
        return classPeriodRepository.findAll();
    }






    /**
     * Update an existing ClassPeriod.
     *
     * <p>This method validates any updated foreign key IDs against Django.
     * Partial updates are supported; fields that are {@code null} in the request
     * are left unchanged.</p>
     *
     * @param id  the ID of the ClassPeriod to update
     * @param req request DTO containing fields to update; may contain nulls for partial update
     * @return the updated {@link ClassPeriod}
     * @throws ResponseStatusException 404 if ClassPeriod not found
     * @throws ResponseStatusException 400 if provided foreign key IDs are invalid
     * @throws ResponseStatusException 502 if Django is unreachable
     */
    @Transactional
    public ClassPeriod updateClassPeriod(Long id, ClassPeriodRequest req) {
        ClassPeriod existing = classPeriodRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "ClassPeriod not found with id: " + id));

        // If the request provides any of the Django-backed foreign keys, validate them.
        boolean needsValidation = false;
        if (req.getSubjectId() != null) needsValidation = true;
        if (req.getYearLevelId() != null) needsValidation = true;
        if (req.getTeacherId() != null) needsValidation = true;
        if (req.getTermId() != null) needsValidation = true;

        if (needsValidation) {
            // Build a validation DTO that uses either the new id (if provided) or existing one.
            ClassPeriodRequest validationDto = new ClassPeriodRequest();
            validationDto.setSubjectId(req.getSubjectId() != null ? req.getSubjectId() : existing.getSubjectId());
            validationDto.setYearLevelId(req.getYearLevelId() != null ? req.getYearLevelId() : existing.getYearLevelId());
            validationDto.setTeacherId(req.getTeacherId() != null ? req.getTeacherId() : existing.getTeacherId());
            validationDto.setTermId(req.getTermId() != null ? req.getTermId() : existing.getTermId());

            validateForeignKeys(validationDto);
        }

        // Apply non-null updates (partial update)
        if (req.getSubjectId() != null) existing.setSubjectId(req.getSubjectId());
        if (req.getYearLevelId() != null) existing.setYearLevelId(req.getYearLevelId());
        if (req.getTeacherId() != null) existing.setTeacherId(req.getTeacherId());
        if (req.getTermId() != null) existing.setTermId(req.getTermId());
        if (req.getStartTimeId() != null) existing.setStartTimeId(req.getStartTimeId());
        if (req.getEndTimeId() != null) existing.setEndTimeId(req.getEndTimeId());
        if (req.getClassroomId() != null) existing.setClassroomId(req.getClassroomId());
        if (req.getName() != null) existing.setName(req.getName());

        return classPeriodRepository.save(existing);
    }




    /**
     * Delete a ClassPeriod by ID.
     *
     * @param id the ID of the ClassPeriod to delete
     * @throws ResponseStatusException 404 if not found
     */
    @Transactional
    public void deleteClassPeriod(Long id) {
        if (!classPeriodRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ClassPeriod not found with id: " + id);
        }
        classPeriodRepository.deleteById(id);
    }





//    /**
//     * Forward assignment request to Django using Feign client.
//     *
//     * <p>Returns Django response as a map (expected to be a JSON object).</p>
//     *
//     * @param request the assign request containing yearLevelName and classPeriodNames
//     * @return map of response values returned by Django
//     * @throws ResponseStatusException 502 if Django unreachable or Feign error
//     */
//    public Map<String, Object> assignToYearLevel(AssignRequest request) {
//        try {
//            return assignToYearLevel.assignToYearLevel(request);
//        } catch (FeignException e) {
//            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
//                    "Failed to contact Django service: " + extractFeignMessage(e));
//        }
//    }
}
