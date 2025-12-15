package com.java.sms.serviceImpl;

import com.java.sms.DataClass.ApplyDiscountRequest;
import com.java.sms.DataClass.UpdateDiscountRequest;
import com.java.sms.exception.ApiException;
import com.java.sms.model.AppliedFeeDiscount;
import com.java.sms.model.FeeStructure;
import com.java.sms.model.enums.FeeStatus;
import com.java.sms.openFeignClient.StudentYearClient;
import com.java.sms.openFeignClient.YearLevelClient;
import com.java.sms.repository.AppliedFeeDiscountRepository;
import com.java.sms.repository.FeeStructureRepository;
import com.java.sms.repository.StudentFeeRepository;
import com.java.sms.response.AppliedFeeDiscountResponse;
import com.java.sms.response.StudentYearLevelResponse;
import com.java.sms.response.YearLevelResponse;
import com.java.sms.service.AppliedFeeDiscountService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AppliedFeeDiscount service implementation that validates using Django YearLevel and StudentYearLevel endpoints.
 *
 * Assumptions:
 * - FeeStructure stores Set<Long> yearLevelIds (Django PKs).
 * - StudentYearClient.getStudentYearLevel(id) returns StudentYearLevelResponse with safeLevelName().
 * - YearLevelClient.getYearLevelById(id) returns YearLevelResponse with getLevelName().
 */
@Service
@Slf4j
public class AppliedFeeDiscountServiceImpl implements AppliedFeeDiscountService {

    private final AppliedFeeDiscountRepository discountRepo;
    private final StudentYearClient studentYearClient;
    private final FeeStructureRepository feeStructureRepo;
    private final StudentFeeRepository studentFeeRepo;
    private final YearLevelClient yearLevelClient;

    /**
     * Simple in-memory cache to avoid repeated Feign calls for the same YearLevel id.
     * Key: yearLevelId, Value: levelName
     */
    // Thread-safe map used as a tiny read cache for year-level names
    private final ConcurrentHashMap<Long, String> yearLevelNameCache = new ConcurrentHashMap<>();

    // Constructor: dependency injection for repositories and feign clients
    public AppliedFeeDiscountServiceImpl(
            AppliedFeeDiscountRepository discountRepo,
            StudentYearClient studentYearClient,
            FeeStructureRepository feeStructureRepo,
            StudentFeeRepository studentFeeRepo,
            YearLevelClient yearLevelClient
    ) {

        this.discountRepo = discountRepo;
        this.studentYearClient = studentYearClient;
        this.feeStructureRepo = feeStructureRepo;
        this.studentFeeRepo = studentFeeRepo;
        this.yearLevelClient = yearLevelClient;
    }



    // List endpoint behaviour: when studentYearId provided return available + applied, otherwise return all discounts
    @Override
    public Object list(Long studentYearId) {

        // if studentYearId provided then provide combined object
        if (studentYearId != null) {

            // ordered map for predictable JSON order
            Map<String, Object> out = new LinkedHashMap<>();
            // available fees for the student year
            out.put("available_fees", getAvailableFees(studentYearId));
            // applied discounts for the student year
            out.put("applied_discounts", getAppliedDiscounts(studentYearId));
            // return composed object
            return out;

        }
        else {
            // no studentYearId: return all discounts converted to response DTOs
            return discountRepo.findAll().stream().map(this::toResponse).toList();
        }

    }

    // Fetch applied discounts for a student year and convert to response objects
    @Override
    public List<AppliedFeeDiscountResponse> getAppliedDiscounts(Long studentYearId) {
        // query repository by studentYearId
        var discounts = discountRepo.findByStudentYearId(studentYearId);
        // map each entity to response DTO
        return discounts.stream().map(this::toResponse).toList();
    }



    // Build a list of available fees (pending/partial) for a student year with minimal student info
    @Override
    public List<Map<String, Object>> getAvailableFees(Long studentYearId) {

        // fetch student fees in pending/partial status (repository method should exist)
        var fees = studentFeeRepo.findByStudentYearIdAndStatusIn(
                studentYearId,
                Arrays.asList(
                        FeeStatus.PENDING,
                        FeeStatus.PARTIAL
                )
        );

        // prepare result list
        List<Map<String, Object>> result = new ArrayList<>();


        // iterate over each fee and build a map similar to Django serializer output
        for (var fee : fees) {
            // ordered map for each fee record
            Map<String, Object> m = new LinkedHashMap<>();
            // include student_fee id
            m.put("student_fee_id", fee.getId());
            // include referenced student_year id (Django PK)
            m.put("student_year_id", fee.getStudentYearId());
            // placeholder for student class (populated below via feign)
            m.put("student_class", null);
            // placeholder for school year (populated below via feign)
            m.put("school_year", null);
            // fee type name from local FeeStructure enum/name
            m.put("fee_type", fee.getFeeStructure() != null ? fee.getFeeStructure().getFeeType().name() : null);
            // original amount as double (Django returns floats)
            m.put("original_amount", fee.getOriginalAmount() != null ? fee.getOriginalAmount().doubleValue() : 0.0);
            // due amount as double
            m.put("due_amount", fee.getDueAmount() != null ? fee.getDueAmount().doubleValue() : 0.0);

            try {
                // call Django service to get student-year details
                StudentYearLevelResponse sy = studentYearClient.getStudentYearLevel(fee.getStudentYearId());
                // if response returned map the safe helpers to fields
                if (sy != null) {
                    m.put("student_name", sy.safeStudentName());
                    m.put("student_class", sy.safeLevelName());
                    m.put("school_year", sy.safeYearName());
                } else {
                    // fallback when feign returned null
                    m.put("student_name", "N/A");
                }
            } catch (Exception ex) {
                // log but do not fail the whole operation — best-effort approach
                log.warn("Failed to fetch StudentYear (id={}): {}", fee.getStudentYearId(), ex.getMessage());
                // graceful fallback value
                m.put("student_name", "N/A");
            }

            // add built map to results
            result.add(m);
        }

        // return list of available fees for the student year
        return result;
    }









    // Apply discount endpoint: validate inputs, calculate discount amount and persist
    @Override
    @Transactional
    public AppliedFeeDiscountResponse applyDiscount(ApplyDiscountRequest req, String currentUsername) {

        // placeholder for StudentYearLevelResponse fetched via Feign
        StudentYearLevelResponse sy;

        try {
            // fetch student-year from Django for validation
            sy = studentYearClient.getStudentYearLevel(req.getStudentYearId());

        } catch (Exception e) {

            // log the failure and return a 502-style ApiException
            log.warn("Feign call failed while fetching StudentYearLevel id={}: {}", req.getStudentYearId(), e.getMessage());

            // bubble up as Bad Gateway - cannot validate with Django
            throw new ApiException("Failed to validate student_year_id with Django.", HttpStatus.BAD_GATEWAY);

        }

        // if Django returned null treat as not found
        if (sy == null) {

            throw new ApiException("Invalid student_year_id.", HttpStatus.NOT_FOUND);

        }


        // Validate local FeeStructure exists, otherwise 404
        FeeStructure feeStruct = feeStructureRepo.findById(req.getFeeStructureId())
                .orElseThrow(() -> new ApiException("Invalid fee_structure_id.", HttpStatus.NOT_FOUND));


        // extract level name from StudentYear response (safe accessor)
        String studentLevelName = sy.safeLevelName();

        // if student's level missing treat as bad request
        if (studentLevelName == null || studentLevelName.isBlank()) {

            throw new ApiException("Student level missing in Django response for id: "
                    + req.getStudentYearId(), HttpStatus.BAD_REQUEST);
        }



        // Validate that fee applies to student's level using yearLevelIds -> YearLevelClient
        if (!feeAppliesToLevelByIds(feeStruct, studentLevelName)) {

            throw new ApiException("Discount can only be applied to fees of the student's own class: "
                    + studentLevelName,
                    HttpStatus.BAD_REQUEST);
        }

        // Validate percentage: use provided percent or zero if missing
        BigDecimal pct = Optional.ofNullable(req.getDiscountedAmountPercent()).orElse(BigDecimal.ZERO);

        // ensure percent inside 0..100
        if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(new BigDecimal("100")) > 0) {

            throw new ApiException("Discount percentage must be between 0 and 100.", HttpStatus.BAD_REQUEST);
        }

        // apply an additional max allowed guard (business rule)
        BigDecimal maxAllowed = new BigDecimal("90");

        // if requested percent greater than allowed, reject
        if (pct.compareTo(maxAllowed) > 0) {
            throw new ApiException("Discount cannot exceed " + maxAllowed + "%.", HttpStatus.BAD_REQUEST);
        }


        // compute final discount amount as feeAmount * pct / 100 with 2 decimal places
        BigDecimal feeAmount = Optional.ofNullable(feeStruct.getFeeAmount()).orElse(BigDecimal.ZERO);
        BigDecimal finalDiscountAmount = feeAmount.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        // ensure computed discount is not larger than fee (sanity)
        if (finalDiscountAmount.compareTo(feeAmount) > 0) {

            throw new ApiException("Discount amount cannot be greater than the fee amount.", HttpStatus.BAD_REQUEST);
        }



        // Prevent duplicate discount record for same student-year + fee-structure
        if (discountRepo.existsByStudentYearIdAndFeeStructure(req.getStudentYearId(), feeStruct)) {
            throw new ApiException("Discount already applied for this student and fee type.", HttpStatus.BAD_REQUEST);
        }

        // Build AppliedFeeDiscount entity using builder
        AppliedFeeDiscount d = AppliedFeeDiscount.builder()
                .studentYearId(req.getStudentYearId())
                .feeStructure(feeStruct)
                .discountName(req.getDiscountName())
                .discountAmount(finalDiscountAmount)
                .approvedAt(OffsetDateTime.now())
                .build();

        // Save entity
        var saved = discountRepo.save(d);
        // convert to response DTO
        var resp = toResponse(saved);
        // enrich with student data from Django response
        resp.setStudentName(sy.safeStudentName());
        // set approver name (fallback to Admin)
        resp.setApprovedBy(currentUsername != null ? currentUsername : "Admin");
        // set percent provided in response for convenience
        resp.setDiscountedAmountPercent(pct.doubleValue());
        // return response DTO
        return resp;
    }








    // Update existing discount record with new values, validations similar to apply
    @Override
    @Transactional
    public AppliedFeeDiscountResponse updateDiscount(Long id, UpdateDiscountRequest req, String currentUsername) {

        // fetch existing discount or 404
        AppliedFeeDiscount existing = discountRepo.findById(id)
                .orElseThrow(() -> new ApiException("Discount not found with id: " + id, HttpStatus.NOT_FOUND));


        // use provided percent or 0
        BigDecimal pct = Optional.ofNullable(req.getDiscountedAmountPercent()).orElse(BigDecimal.ZERO);

        // validate percent 0..100
        if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(new BigDecimal("100")) > 0) {

            throw new ApiException("Discount percentage must be between 0 and 100.", HttpStatus.BAD_REQUEST);
        }



        // maximum update percent narrower (business rule)
        BigDecimal maxUpdatePercent = new BigDecimal("80");

        // reject if above allowed update percent
        if (pct.compareTo(maxUpdatePercent) > 0) {

            throw new ApiException("Discount cannot exceed "
                    + maxUpdatePercent + "% on update.", HttpStatus.BAD_REQUEST);
        }


        // resolve fee amount safely from existing relation
        BigDecimal feeAmount = existing.getFeeStructure() != null && existing.getFeeStructure().getFeeAmount() != null
                ? existing.getFeeStructure().getFeeAmount() : BigDecimal.ZERO;

        // compute final discount amount with rounding
        BigDecimal finalDiscountAmount = feeAmount.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);


        // set updated fields on entity
        existing.setDiscountName(req.getDiscountName());
        existing.setDiscountAmount(finalDiscountAmount);
        existing.setApprovedAt(OffsetDateTime.now());

        // persist updated entity
        var saved = discountRepo.save(existing);
        // convert to response DTO
        var resp = toResponse(saved);

        // set percent and approver info in response
        resp.setDiscountedAmountPercent(pct.doubleValue());
        resp.setApprovedBy(currentUsername != null ? currentUsername : "Admin");
        // return DTO
        return resp;


    }








    // ---------------- Helper methods ----------------

    // Convert AppliedFeeDiscount entity to response DTO
    private AppliedFeeDiscountResponse toResponse(AppliedFeeDiscount d) {

        // default percent to 0.0
        Double pct = 0.0;

        // calculate percentage only if feeAmount available and > 0
        if (d.getFeeStructure() != null && d.getFeeStructure().getFeeAmount() != null
                && d.getFeeStructure().getFeeAmount().compareTo(BigDecimal.ZERO) > 0) {

            // percent = discountAmount / feeAmount * 100 (high precision then to double)
            pct = d.getDiscountAmount()
                    .divide(d.getFeeStructure().getFeeAmount(), 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }


        // return built response DTO using builder pattern
        return AppliedFeeDiscountResponse.builder()
                .id(d.getId())
                .StudentYearLevel(d.getStudentYearId())
                .feeTypeId(d.getId() != null ? d.getId() : null)
                .feeTypeName(d.getFeeStructure() != null ? d.getFeeStructure().getFeeType().name() : null)
                .discountName(d.getDiscountName())
                .discountAmount(d.getDiscountAmount())
                .discountedAmountPercent(pct)
                .approvedBy(d.getApprovedBy() != null ? AuthServiceImpl.getFullName(d.getApprovedBy()) : null)
                .approvedAt(d.getApprovedAt())
                .build();


    }










    /**
     * Validate FeeStructure by checking its yearLevelIds against student's level name.
     * Uses YearLevelClient to fetch name by id, cached locally in yearLevelNameCache.
     */
    // Check whether the given FeeStructure applies to a student level (by comparing names)
    private boolean feeAppliesToLevelByIds(FeeStructure feeStruct, String studentLevelName) {

        // null safety for feeStruct
        if (feeStruct == null)
            return false;

        // null/blank safety for studentLevelName
        if (studentLevelName == null || studentLevelName.isBlank())
            return false;



        // fetch year-level id set from fee structure
        Set<Long> ids = feeStruct.getYearLevelIds();
        // if missing ids then it's a configuration error
        if (ids == null || ids.isEmpty()) {

            throw new ApiException("FeeStructure missing year-level ids." +
                    " Provide yearLevelIds on FeeStructure.",

                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // ensure YearLevelClient present to validate remote names
        if (yearLevelClient == null) {

            throw new ApiException("YearLevelClient not configured." +
                    " Required to validate FeeStructure yearLevelIds.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        // iterate ids and try to find matching name
        for (Long ylId : ids) {

            try {

                // use cache to reduce Feign calls; computeIfAbsent will call feign on cache miss
                String levelName = yearLevelNameCache.computeIfAbsent(ylId, id -> {

                    try {
                        // fetch YearLevel from Django
                        YearLevelResponse yl = yearLevelClient.getYearLevelById(id);
                        // return name or null when missing
                        return yl != null ? yl.getLevelName() : null;

                    } catch (Exception ex) {
                        // log at debug level and return null to continue other ids
                        log.debug("Failed to fetch YearLevel id={} from Django: {}", id, ex.getMessage());
                        return null;
                    }

                });


                // if fetched name equals student level name (case-insensitive) return true
                if (levelName != null && levelName.equalsIgnoreCase(studentLevelName)) {
                    return true;
                }


            } catch (Exception e) {
                // non-fatal: log and continue with other ids
                log.debug("Error while validating yearLevel id {}: {}", ylId, e.getMessage());
            }

        }

        // no match found after checking all ids
        return false;

    }
}
