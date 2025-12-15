package com.java.sms.serviceImpl;


import com.java.sms.DataClass.*;
import com.java.sms.exception.ApiException;
import com.java.sms.model.AppliedFeeDiscount;
import com.java.sms.model.FeePayment;
import com.java.sms.model.FeeStructure;
import com.java.sms.model.enums.FeeStatus;
import com.java.sms.model.enums.PaymentMethod;
import com.java.sms.model.enums.PaymentStatus;
import com.java.sms.openFeignClient.SchoolYearClient;
import com.java.sms.openFeignClient.StudentYearClient;
import com.java.sms.openFeignClient.YearLevelClient;
import com.java.sms.repository.*;
import com.java.sms.response.ConfirmPaymentResponse;
import com.java.sms.response.StudentYearLevelResponse;
import com.java.sms.response.YearLevelResponse;
import com.java.sms.service.RazorpayService;
import com.java.sms.service.StudentFeeService;
import jakarta.transaction.Transactional;
import com.java.sms.model.StudentFee;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.java.sms.response.InitiatePaymentResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation that mirrors your Django logic.
 * - Validates StudentYearLevel and SchoolYear via Feign (Django)
 * - Ensures FeeStructure belongs to student's level
 * - Applies discounts (AppliedFeeDiscount repository)
 * - Creates/updates StudentFee records
 * - Initiates Razorpay orders (createOrder)
 * - Confirms payments (verify signature + create FeePayment entries)
 *
 * Marked @Transactional to ensure atomic updates for payment confirm flow.
 */
@Service
@Transactional
public class StudentFeeServiceImpl implements StudentFeeService {



    private final StudentFeeRepository studentFeeRepo;
    private final FeeStructureRepository feeStructureRepo;
    private final AppliedFeeDiscountRepository discountRepo;
    private final FeePaymentRepository feePaymentRepo;
    private final StudentYearClient studentYearClient;
    private final SchoolYearClient schoolYearClient;
    private final RazorpayService razorpayService;
    private final YearLevelClient yearLevelClient;

    public StudentFeeServiceImpl(StudentFeeRepository studentFeeRepo,
                                 FeeStructureRepository feeStructureRepo,
                                 AppliedFeeDiscountRepository discountRepo,
                                 FeePaymentRepository feePaymentRepo,
                                 StudentYearClient studentYearClient,
                                 SchoolYearClient schoolYearClient,
                                 RazorpayService razorpayService, YearLevelClient yearLevelClient) {
        this.studentFeeRepo = studentFeeRepo;
        this.feeStructureRepo = feeStructureRepo;
        this.discountRepo = discountRepo;
        this.feePaymentRepo = feePaymentRepo;
        this.studentYearClient = studentYearClient;
        this.schoolYearClient = schoolYearClient;
        this.razorpayService = razorpayService;
        this.yearLevelClient = yearLevelClient;
    }









    @Override
    public Object createOrUpdateStudentFee(StudentFeeRequest req) {

        // 1) Fetch StudentYearLevel from Django
        var studentYear = studentYearClient.getStudentYearLevel(req.getStudentYearId());

        if (studentYear == null) {
            throw new ApiException("StudentYearLevel not found: " + req.getStudentYearId(),
                    HttpStatus.NOT_FOUND);
        }

        // 2) Resolve YearLevel ID (Django returns only level_name)
        Long studentLevelId = resolveYearLevelId(studentYear);

        // 3) Validate FeeStructure
        var feeStructure = feeStructureRepo.findById(req.getFeeStructureId())
                .orElseThrow(() -> new ApiException("FeeStructure not found: " +
                        req.getFeeStructureId(), HttpStatus.NOT_FOUND));

        // 4) Validate SchoolYear (Django)
        var schoolYear = schoolYearClient.getSchoolYear(req.getSchoolYearId());
        if (schoolYear == null) {
            throw new ApiException("SchoolYear not found: " + req.getSchoolYearId(), HttpStatus.NOT_FOUND);
        }

        // 5) Ensure fee belongs to student's class
        if (!feeStructure.getYearLevelIds().contains(studentLevelId)) {
            throw new ApiException("Selected fee does not belong to the student's class: "
                    + studentYear.getLevelName(), HttpStatus.BAD_REQUEST);
        }

        // 6) Check for discount (optional)
        Optional<AppliedFeeDiscount> appliedDiscountOpt =
                discountRepo.findFirstByStudentYearIdAndFeeStructure(req.getStudentYearId(), feeStructure);

        BigDecimal discountAmount = appliedDiscountOpt
                .map(AppliedFeeDiscount::getDiscountAmount)
                .orElse(BigDecimal.ZERO);

        BigDecimal discountedAmount = feeStructure.getFeeAmount()
                .subtract(discountAmount)
                .max(BigDecimal.ZERO);

        // 7) Find existing StudentFee
        Optional<StudentFee> existingOpt =
                studentFeeRepo.findByStudentYearIdAndFeeStructureIdAndMonthAndSchoolYearId(
                        req.getStudentYearId(), req.getFeeStructureId(),
                        req.getMonth(), req.getSchoolYearId());

        StudentFee studentFee;

        if (existingOpt.isPresent()) {

            studentFee = existingOpt.get();

            // apply discount if not applied earlier
            if (appliedDiscountOpt.isPresent() && !studentFee.getAppliedDiscount()) {

                studentFee.setOriginalAmount(discountedAmount);

                studentFee.setDueAmount(
                        discountedAmount
                                .subtract(studentFee.getPaidAmount())
                                .max(BigDecimal.ZERO)
                );

                studentFee.setAppliedDiscount(true);
            }

        } else {

            studentFee = StudentFee.builder()
                    .studentYearId(req.getStudentYearId())
                    .feeStructure(feeStructure)
                    .schoolYearId(req.getSchoolYearId())
                    .month(req.getMonth())
                    .originalAmount(discountedAmount)
                    .paidAmount(BigDecimal.ZERO)
                    .dueAmount(discountedAmount)
                    .penaltyAmount(BigDecimal.ZERO)
                    .appliedDiscount(appliedDiscountOpt.isPresent())
                    .status(FeeStatus.PENDING)
                    .receiptNumber(generateReceipt())
                    .dueDate(req.getDueDate())
                    .build();
        }

        // 8) Tuition fee penalty logic
        if ("TUITION_FEE".equalsIgnoreCase(feeStructure.getFeeType().name())) {

            LocalDate today = LocalDate.now();

            if (studentFee.getDueDate() != null
                    && today.isAfter(studentFee.getDueDate())
                    && !studentFee.getAppliedDiscount()) {

                studentFee.setPenaltyAmount(new BigDecimal("25.00"));
                studentFee.setAppliedDiscount(true);
            }
            else {
                studentFee.setPenaltyAmount(BigDecimal.ZERO);
            }
        }

        // 9) Payment handling
        BigDecimal amountPaid = req.getAmountPaid() != null ? req.getAmountPaid() : BigDecimal.ZERO;

        if (amountPaid.compareTo(studentFee.getDueAmount()) > 0) {
            throw new ApiException("Amount cannot exceed due amount: " + studentFee.getDueAmount(),
                    HttpStatus.BAD_REQUEST);
        }

        String pm = req.getPaymentMethod() != null ? req.getPaymentMethod().toLowerCase() : null;

        if ("online".equals(pm)) {
            studentFee.setStatus(FeeStatus.PENDING);
        } else {

            studentFee.setPaidAmount(studentFee.getPaidAmount().add(amountPaid));

            studentFee.setDueAmount(
                    studentFee.getOriginalAmount()
                            .subtract(studentFee.getPaidAmount())
                            .add(studentFee.getPenaltyAmount())
                            .max(BigDecimal.ZERO)
            );

            if (studentFee.getDueAmount().compareTo(BigDecimal.ZERO) <= 0) {
                studentFee.setStatus(FeeStatus.PAID);
            } else if (studentFee.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                studentFee.setStatus(FeeStatus.PARTIAL);
            } else {
                studentFee.setStatus(FeeStatus.PENDING);
            }
        }

        return studentFeeRepo.save(studentFee);
    }




    @Override
    @Transactional
    public Object submitFee(SubmitFeeRequest req) throws Exception {

        // Validate required fields: studentYearId and fees list must be present (same as Django check)
        if (req.getStudentYearId() == null || req.getFees() == null || req.getFees().isEmpty()) {
            throw new ApiException("student_year_id and fees are required", HttpStatus.BAD_REQUEST);
        }

        // verify student-year exists (Feign) — equivalent to StudentYearLevel.objects.get(...) in Django
        var studentYear = studentYearClient.getStudentYearLevel(req.getStudentYearId());
        if (studentYear == null)
            throw new ApiException("StudentYearLevel not found", HttpStatus.BAD_REQUEST);

        // determine payment mode, defaulting to CASH (mirrors Django's default ""), here using enum PaymentMethod
        PaymentMethod paymentMode = req.getPaymentMethod() != null
                ? req.getPaymentMethod() : PaymentMethod.CASH;

        // read cheque number from request (same as Django `cheque_number = request.data.get("cheque_number")`)
        String chequeNumber = req.getChequeNumber();

        // school year from request (same as Django `school_year_id = request.data.get("school_year_id")`)
        Long schoolYearIdFromReq = req.getSchoolYearId();

        // accumulator for total paid amount and created/updated student fee records
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<StudentFee> created = new ArrayList<>();

        // iterate over each fee item from request (maps to `for fee_data in fees_data:`)
        for (Map<String,Object> feeData : req.getFees()) {

            // normalize/accept both "fee_id" and "fee_type_id" keys (Django used fee_type_id but frontends vary)
            Long feeTypeId = null;
            if (feeData.get("fee_id") != null)
                feeTypeId = ((Number) feeData.get("fee_id")).longValue();
            else if (feeData.get("fee_type_id") != null)
                feeTypeId = ((Number) feeData.get("fee_type_id")).longValue();
            if (feeTypeId == null)
                continue; // skip if fee type not provided (Django would error earlier; here we skip)

            // read month (can be null) and due_date string if provided
            Integer month = feeData.get("month") != null
                    ? ((Number) feeData.get("month")).intValue() : null;

            String dueDateStr = feeData.get("due_date") != null
                    ? String.valueOf(feeData.get("due_date")) : null;

            // parse amount paid; scale to 2 decimals (mirrors Django Decimal quantize)
            BigDecimal amountPaid = feeData.get("amount") != null
                    ? new BigDecimal(String.valueOf(feeData.get("amount"))).setScale(2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;

            // load FeeStructure (equivalent to Django fee_structure lookup/serializer relationship)
            FeeStructure feeStruct = feeStructureRepo.findById(feeTypeId).orElse(null);
            if (feeStruct == null)
                continue; // skip if structure not found (Django's serializer would validate)

            // choose school year id to use for StudentFee; could be null — same concept as Django using school_year_id
            Long schoolYearToUse = schoolYearIdFromReq;

            // Try find an existing StudentFee by studentYear, feeStructure, month, schoolYear.
            // This is a difference vs Django if its serializer always creates new.
            Optional<StudentFee> sfOpt = studentFeeRepo
                    .findByStudentYearIdAndFeeStructureIdAndMonthAndSchoolYearId(
                            req.getStudentYearId(), feeTypeId, month, schoolYearToUse
                    );

            StudentFee studentFee;

            if (sfOpt.isPresent()) {
                // reuse existing studentFee — update its fields later (avoids duplicates).
                studentFee = sfOpt.get();
            }
            else {
                // create a new StudentFee record (fill initial amounts and defaults)
                studentFee = StudentFee.builder()
                        .studentYearId(req.getStudentYearId())
                        .feeStructure(feeStruct)
                        .schoolYearId(schoolYearToUse)
                        .month(month)
                        .originalAmount(feeStruct.getFeeAmount())
                        .paidAmount(BigDecimal.ZERO)
                        .dueAmount(feeStruct.getFeeAmount())
                        .penaltyAmount(BigDecimal.ZERO)
                        .appliedDiscount(false)
                        .status(FeeStatus.PENDING) // initial status pending
                        .receiptNumber(generateReceipt()) // generate a receipt (Django had serializer or later)
                        .build();

                // persist newly created StudentFee
                studentFee = studentFeeRepo.save(studentFee);
            }

            // set due date from request string if provided, else set default (15th of month current year)
            if (dueDateStr != null && !dueDateStr.isBlank()) {
                studentFee.setDueDate(LocalDate.parse(dueDateStr));
            }
            else if (month != null) {
                int year = LocalDate.now().getYear();
                studentFee.setDueDate(LocalDate.of(year, month, 15));
            }

            // applied discount lookup (equivalent to Django AppliedFeeDiscount.objects.filter(...).first())
            AppliedFeeDiscount discountObj = discountRepo
                    .findFirstByStudentYearIdAndFeeStructure(req.getStudentYearId(), feeStruct)
                    .orElse(null);

            // discount amount numeric (0 if none) and mark appliedDiscount boolean
            BigDecimal discountAmount = discountObj != null
                    ? discountObj.getDiscountAmount() : BigDecimal.ZERO;
            studentFee.setAppliedDiscount(discountObj != null);

            // compute maximum payable after discount minus already paid amount
            BigDecimal maxPayable = studentFee.getOriginalAmount()
                    .subtract(discountAmount)
                    .subtract(studentFee.getPaidAmount() != null
                            ? studentFee.getPaidAmount() : BigDecimal.ZERO)
                    .max(BigDecimal.ZERO);

            // validate amountPaid does not exceed max payable (mirrors Django check)
            if (amountPaid.compareTo(maxPayable) > 0) {
                throw new ApiException("Amount cannot exceed due amount after discount: " + maxPayable,
                        HttpStatus.BAD_REQUEST);
            }

            // create payment record and relate to studentFee (maps to FeePayment.objects.create(...) in Django)
            FeePayment payment = new FeePayment();
            payment.setStudentFee(studentFee); // ManyToOne relation
            payment.setAmount(amountPaid);
            payment.setPaymentMethod(paymentMode); // enum (better than plain string)
            payment.setChequeNumber(chequeNumber);
            // set status — if online then pending, else success (Django used "initiated" for online)
            payment.setStatus(paymentMode == PaymentMethod.ONLINE
                    ? PaymentStatus.PENDING : PaymentStatus.SUCCESS);
            // set payment date only for non-online payments (Django uses timezone.now() if not online)
            payment.setPaymentDate(paymentMode == PaymentMethod.ONLINE
                    ? null : LocalDateTime.now());
            feePaymentRepo.save(payment);

            // recompute aggregated paid amount by summing FeePayment.amount for this studentFee
            BigDecimal aggregatedPaid = feePaymentRepo
                    .findByStudentFee(studentFee.getId()).stream()
                    .map(FeePayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            studentFee.setPaidAmount(aggregatedPaid);

            // penalty logic: if fee type is "tuition fee" and due_date passed, apply fixed 25.00 penalty
            // (Django used fee_structure.fee_type.lower() == "tution fee" — check spelling)
            if ("tuition fee".equalsIgnoreCase(feeStruct.getFeeType().name())
                    && studentFee.getDueDate() != null
                    && LocalDate.now().isAfter(studentFee.getDueDate())) {

                studentFee.setPenaltyAmount(new BigDecimal("25.00"));
            }
            else {
                studentFee.setPenaltyAmount(BigDecimal.ZERO);
            }

            // compute dueAmount = original - paid - discount + penalty, then max(0)
            studentFee.setDueAmount(
                    studentFee.getOriginalAmount()
                            .subtract(studentFee.getPaidAmount() != null
                                    ? studentFee.getPaidAmount() : BigDecimal.ZERO)
                            .subtract(discountAmount)
                            .add(studentFee.getPenaltyAmount() != null
                                    ? studentFee.getPenaltyAmount() : BigDecimal.ZERO)
                            .max(BigDecimal.ZERO)
            );

            // status update:
            // - if online payment mode: keep studentFee status PENDING
            // - else: set PAID if dueAmount <= 0, PARTIAL if paidAmount > 0, else PENDING
            if (paymentMode == PaymentMethod.ONLINE) {
                studentFee.setStatus(FeeStatus.PENDING);
            }
            else {
                if (studentFee.getDueAmount().compareTo(BigDecimal.ZERO) <= 0)
                    studentFee.setStatus(FeeStatus.PAID);
                else if (studentFee.getPaidAmount().compareTo(BigDecimal.ZERO) > 0)
                    studentFee.setStatus(FeeStatus.PARTIAL);
                else
                    studentFee.setStatus(FeeStatus.PENDING);
            }

            // persist updates to StudentFee
            studentFeeRepo.save(studentFee);

            // aggregate totals and collect created/updated records for final response
            totalAmount = totalAmount.add(amountPaid);
            created.add(studentFee);

        } // end for

        // If payment mode is ONLINE, create Razorpay order / return initiation response
        if (paymentMode == PaymentMethod.ONLINE) {

            // minimum amount guard (Java requires >= 1 INR)
            if (totalAmount.compareTo(new BigDecimal("1.00")) < 0) {
                throw new ApiException("Paid amount must be at least 1 INR to create Razorpay order.",
                        HttpStatus.BAD_REQUEST);
            }

            String receipt = generateReceipt();
            Map<String,Object> order = razorpayService
                    .createOrder(totalAmount.multiply(new BigDecimal(100)).longValue(),
                            "INR", receipt);

            Map<String,Object> resp = new HashMap<>();
            resp.put("message", "Payment initiated successfully - status pending.");
            resp.put("razorpay_order_id", order.get("id"));
            resp.put("receipt_number", receipt);

            // include fees summary in response (id, fee_type, status, due_amount, month)
            resp.put("fees", created.stream().map(sf -> {
                Map<String,Object> map = new HashMap<>();
                map.put("id", sf.getId());
                map.put("fee_type", sf.getFeeStructure().getFeeType().name());
                map.put("status", sf.getStatus().name());
                map.put("due_amount", sf.getDueAmount().toPlainString());
                map.put("month", sf.getMonth());
                return map;
            }).collect(Collectors.toList()));

            return resp;
        }

        // Offline payments: build response list (like Django's output_serializer.data)
        List<Map<String,Object>> out = created.stream().map(sf -> {
            Map<String,Object> map = new HashMap<>();
            map.put("id", sf.getId());
            map.put("fee_type", sf.getFeeStructure().getFeeType().name());
            map.put("status", sf.getStatus().name());
            map.put("due_amount", sf.getDueAmount().toPlainString());
            map.put("month", sf.getMonth());
            return map;
        }).collect(Collectors.toList());

        Map<String,Object> resp = new HashMap<>();
        resp.put("message", created.size() + " fee records submitted successfully!");
        resp.put("total_amount_paid", totalAmount.toPlainString());
        resp.put("payment_mode", paymentMode.name());
        resp.put("data", out);
        return resp;
    }


    @Override
    public List<FeePreviewItem> previewFees(Long studentYearId) {

        // Fetch student-year info from Django microservice via Feign client.
        // (Django method uses StudentYearLevel.objects.get(id=student_year_id))
        var studentYear = studentYearClient.getStudentYearLevel(studentYearId);

        // If remote service returned nothing, we raise an error (Django returns 404 earlier).
        if (studentYear == null) {
            throw new com.java.sms.exception.ApiException(
                    "StudentYearLevel service returned no data for id: " + studentYearId,
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
            );
        }

        // Attempt to resolve the YearLevel ID. Django already had `student_year_level.level`.
        Long yearLevelId = null;

    /* Backwards-compatible: if DTO contains nested level object you could extract it here.
       (commented-out example in code) */

        // If we still don't have an ID, fall back to resolving by level name (level_name).
        if (yearLevelId == null) {
            String levelName = studentYear.getLevelName(); // maps to Django `level_name`
            if (levelName == null || levelName.isBlank()) {
                // If levelName missing, we can't resolve fee structures — error out.
                throw new com.java.sms.exception.ApiException(
                        "StudentYearLevel response missing level info for id: " + studentYearId,
                        org.springframework.http.HttpStatus.BAD_GATEWAY
                );
            }

            // Fetch all YearLevels from Django (via another Feign client)
            List<YearLevelResponse> allLevels;
            try {
                allLevels = yearLevelClient.getAllYearLevels();
            } catch (Exception ex) {
                // If the YearLevel service call fails, return service-unavailable style error.
                throw new com.java.sms.exception.ApiException(
                        "Failed to fetch YearLevel list from Django: " + ex.getMessage(),
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
                );
            }

            if (allLevels == null || allLevels.isEmpty()) {
                throw new com.java.sms.exception.ApiException(
                        "YearLevel service returned empty list while resolving level '" + levelName + "'",
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
                );
            }

            // Case-insensitive match by level name to find the YearLevel id (maps to Django relationship).
            final String wanted = levelName.trim().toLowerCase();
            var matched = allLevels.stream()
                    .filter(y -> y.getLevelName() != null && y.getLevelName().trim().toLowerCase().equals(wanted))
                    .findFirst();

            if (matched.isEmpty()) {
                // If can't resolve, return a clear error explaining the mismatch.
                throw new com.java.sms.exception.ApiException(
                        "Could not find YearLevel with name '" + levelName + "' in Django. " +
                                "Ensure YearLevel exists or StudentYearLevel returns an ID.",
                        org.springframework.http.HttpStatus.BAD_GATEWAY
                );
            }
            // Set resolved ID for later use
            yearLevelId = matched.get().getId();
        }

        // final guard: if still null, throw error (should not happen after above logic)
        if (yearLevelId == null) {
            throw new ApiException("Failed to resolve YearLevel ID for studentYearId=" + studentYearId,
                    HttpStatus.BAD_GATEWAY);
        }

        // Make yearLevelId effectively final for lambdas below
        final Long levelIdFinal = yearLevelId;

        // Load fee structures for that YearLevel.
        // Note: Django used FeeStructure.objects.filter(year_level=year_level)
        // Java uses an approach where FeeStructure contains a list of year-level IDs.
        List<FeeStructure> yearLevelFees = feeStructureRepo.findAll().stream()
                .filter(f -> f.getYearLevelIds() != null && f.getYearLevelIds().contains(levelIdFinal))
                .collect(Collectors.toList());

        // Load all StudentFee records for this student-year (equivalent to Django `paid_fees = StudentFee.objects.filter(student_year=...)`)
        List<StudentFee> paidFees = studentFeeRepo.findByStudentYearId(studentYearId);

        // Output list we will return (month-wise)
        List<FeePreviewItem> result = new ArrayList<>();

        // Iterate months 1..12 (Django enumerated calendar.month_name; same concept)
        for (int m = 1; m <= 12; m++) {

            final int monthFinal = m;  // required because we use lambdas/inner classes below

            // feesForMonth will hold brief objects for every fee type in this month
            List<FeePreviewItem.FeeBrief> feesForMonth = new ArrayList<>();

            // Iterate all fee types applicable to this year-level
            for (FeeStructure fee : yearLevelFees) {

                final FeeStructure feeFinal = fee;  // again final for lambdas

                // Discount lookup:
                // sum all discounts for this student-year + fee structure (matches Django aggregate Sum)
                BigDecimal discountTotal = discountRepo
                        .sumDiscountAmountByStudentYearIdAndFeeStructure(studentYearId, feeFinal);

                if (discountTotal == null)
                    discountTotal = BigDecimal.ZERO; // defensive



                // Compute base amount = fee amount - discount, but not negative.
                BigDecimal baseAmount = feeFinal.getFeeAmount().subtract(discountTotal).max(BigDecimal.ZERO);

                BigDecimal totalPaid;

                // Special-case: Admission fee should be shown only in January:
                // Django checks `fee.fee_type.lower() == "admission fee"` and `month_name != "January"`
                // Java checks enum name `ADMISSION_FEE` and uses month==1.
                if ("ADMISSION_FEE".equalsIgnoreCase(feeFinal.getFeeType().name())) {

                    // If current month isn't January (1) skip this fee type for months other than Jan
                    if (monthFinal != 1) continue;

                    // Sum payments for this fee across all months (admission fee is not month-specific).
                    totalPaid = paidFees.stream()
                            .filter(pf -> pf.getFeeStructure().getId().equals(feeFinal.getId()))
                            .map(StudentFee::getPaidAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                } else {
                    // Non-admission fee: sum paid amounts for this fee structure and this month only.
                    totalPaid = paidFees.stream()
                            .filter(pf -> pf.getFeeStructure().getId().equals(feeFinal.getId())
                                    && Objects.equals(pf.getMonth(), monthFinal))
                            .map(StudentFee::getPaidAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                // Determine status string:
                // - Paid if totalPaid >= baseAmount and baseAmount > 0
                // - Partially Paid if totalPaid > 0
                // - Pending otherwise
                String statusStr;
                if (totalPaid.compareTo(baseAmount) >= 0 && baseAmount.compareTo(BigDecimal.ZERO) > 0)
                    statusStr = "Paid";
                else if (totalPaid.compareTo(BigDecimal.ZERO) > 0)
                    statusStr = "Partially Paid";
                else
                    statusStr = "Pending";

                // Add a FeeBrief entry for this fee in the current month.
                feesForMonth.add(FeePreviewItem.FeeBrief.builder()
                        .feeId(feeFinal.getId())
                        .feeType(feeFinal.getFeeType().name())      // string name of fee type
                        .originalAmount(baseAmount.toPlainString()) // base amount after discount
                        .paidAmount(totalPaid.toPlainString())      // aggregated paid amount
                        .status(statusStr)
                        .appliedDiscount(discountTotal.toPlainString())
                        .build());
            }

            // If any fees exist for the month, add the month entry (Django only appended months with fees too)
            if (!feesForMonth.isEmpty()) {
                // Month.of(m).name() -> returns "JANUARY", "FEBRUARY", ... (Django uses "January")
                result.add(new FeePreviewItem(Month.of(monthFinal).name(), feesForMonth));
            }
        }

        // Return the preview list (matches Django's Response(result))
        return result;
    }







    @Override
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest req) throws Exception {

        if (req.getStudentYearId() == null)
            throw new ApiException("Student_year_id missing", HttpStatus.BAD_REQUEST);

        if (req.getFees() == null || req.getFees().isEmpty())
            throw new ApiException("No fees selected", HttpStatus.BAD_REQUEST);


        var studentYear = studentYearClient.getStudentYearLevel(req.getStudentYearId());

        if (studentYear == null)
            throw new ApiException("Invalid student_year_id", HttpStatus.BAD_REQUEST);


        String receipt = generateReceipt();
        List<Map<String,Object>> created = new ArrayList<>();


        for (Map<String, Object> feeDict : req.getFees()) {

            Long feeId = feeDict.containsKey("fee_id") ? ((Number)feeDict.get("fee_id")).longValue() :
                    feeDict.containsKey("fee_type_id") ? ((Number)feeDict.get("fee_type_id")).longValue() : null;


            Integer month = feeDict.containsKey("month") ? ((Number)feeDict.get("month")).intValue() : null;

            if (feeId == null)
                continue;

            FeeStructure feeObj = feeStructureRepo.findById(feeId).orElse(null);

            if (feeObj == null)
                continue;


            Long schoolYearId = req.getSchoolYearId() != null
                    ? req.getSchoolYearId() : studentYear.getId(); // fallback


            Optional<StudentFee> sfOpt = studentFeeRepo.findByStudentYearIdAndFeeStructureIdAndMonthAndSchoolYearId(
                    req.getStudentYearId(), feeId, month, schoolYearId);

            StudentFee studentFee;

            if (sfOpt.isPresent()) {

                studentFee = sfOpt.get();

            } else {
                studentFee = StudentFee.builder()
                        .studentYearId(req.getStudentYearId())
                        .feeStructure(feeObj)
                        .schoolYearId(schoolYearId)
                        .month(month)
                        .originalAmount(feeObj.getFeeAmount())
                        .paidAmount(BigDecimal.ZERO)
                        .dueAmount(feeObj.getFeeAmount())
                        .status(FeeStatus.PENDING)
                        .receiptNumber(receipt)
                        .build();

                studentFeeRepo.save(studentFee);

            }

            Map<String,Object> createdMap = new HashMap<>();
            createdMap.put("id", studentFee.getId());
            createdMap.put("fee_type", feeObj.getFeeType().name());
            createdMap.put("status", studentFee.getStatus().name());
            createdMap.put("due_amount", studentFee.getDueAmount().toPlainString());
            createdMap.put("month", studentFee.getMonth());
            created.add(createdMap);
        }



        // total amount calculation
        BigDecimal totalAmount = req.getFees().stream()

                .map(mf -> {

                    Object amt = mf.get("amount");

                    if (amt == null) amt = mf.get("paid_amount");

                    return new BigDecimal(String.valueOf(amt == null ? "0" : amt.toString()));

                })

                .reduce(BigDecimal.ZERO, BigDecimal::add);


        if (totalAmount.compareTo(new BigDecimal("1.00")) < 0) {

            throw new ApiException("Paid amount must be at least 1 INR to create Razorpay order.",
                    HttpStatus.BAD_REQUEST);
        }


        Map<String, Object> order = razorpayService
                .createOrder(totalAmount.multiply(new BigDecimal("100"))
                        .longValue(), "INR", receipt);


        String orderId = String.valueOf(order.get("id"));


        return InitiatePaymentResponse.builder()
                .message("Payment initiated successfully - status pending.")
                .razorpayOrderId(orderId)
                .receiptNumber(receipt)
                .fees(created)
                .build();
    }














    @Override
    public ConfirmPaymentResponse confirmPayment(ConfirmPaymentRequest req) {

        // validate required fields
        if (req.getStudentYearId() == null)
            throw new ApiException("student_year_id is required", HttpStatus.BAD_REQUEST);


        if (req.getSelectedFees() == null || req.getSelectedFees().isEmpty())
            throw new ApiException("selected_fees is required", HttpStatus.BAD_REQUEST);


        if (req.getPaymentMode() == null)
            throw new ApiException("payment_mode is required", HttpStatus.BAD_REQUEST);

        if (req.getReceivedBy() == null)
            throw new ApiException("received_by is required", HttpStatus.BAD_REQUEST);

        if (req.getRazorpayOrderId() == null || req.getRazorpayPaymentId() == null
                || req.getRazorpaySignature() == null)

            throw new ApiException("Razorpay fields required", HttpStatus.BAD_REQUEST);


        // verify Razorpay signature BEFORE making DB changes
        boolean ok = razorpayService
                .verifyPaymentSignature(req.getRazorpayOrderId(),
                        req.getRazorpayPaymentId(), req.getRazorpaySignature());



        if (!ok)
            throw new ApiException("Invalid razorpay signature", HttpStatus.BAD_REQUEST);



        var studentYear = studentYearClient.getStudentYearLevel(req.getStudentYearId());

        if (studentYear == null)
            throw new ApiException("Invalid student_year_id", HttpStatus.BAD_REQUEST);


        List<Map<String,Object>> createdPayments = new ArrayList<>();

        for (Map<String,Object> feeItem : req.getSelectedFees()) {
            Long feeId = feeItem.containsKey("fee_id") ? ((Number)feeItem.get("fee_id")).longValue() : null;

            Integer month = feeItem.containsKey("month") ? ((Number)feeItem.get("month")).intValue() : null;

            BigDecimal paidAmount = new BigDecimal(
                    String.valueOf(
                            feeItem.getOrDefault("paid_amount",
                                    feeItem.getOrDefault("amount", "0"))));




            if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {

                throw new ApiException("Paid amount missing or zero for fee_id " + feeId,
                        HttpStatus.BAD_REQUEST);
            }


            FeeStructure feeObj = feeStructureRepo.
                    findById(feeId).orElseThrow(
                            () -> new ApiException("FeeStructure not found for fee_id " + feeId,
                                    HttpStatus.BAD_REQUEST));



            // find student_fee
            List<StudentFee> q = studentFeeRepo.
                    findByStudentYearIdAndFeeStructureId(req.getStudentYearId(), feeId);


            StudentFee studentFee = q.stream().
                    filter(sf -> Objects.equals(sf.getMonth(), month))
                    .findFirst().orElse(null);


            if (studentFee == null) {

                throw new ApiException("StudentFee record not found for fee_id " + feeId + " and month "
                        + month + ". Please initiate payment first.",
                        HttpStatus.BAD_REQUEST);
            }


            if (paidAmount.compareTo(studentFee.getOriginalAmount()) > 0) {
                throw new ApiException("Paid amount cannot exceed original amount ("
                        + studentFee.getOriginalAmount() + ") for fee_id " + feeId,
                        HttpStatus.BAD_REQUEST);
            }


            // update amounts and status
            studentFee.setPaidAmount(studentFee.getPaidAmount().add(paidAmount));

            studentFee.setDueAmount(studentFee.getOriginalAmount()
                    .subtract(studentFee.getPaidAmount()).max(BigDecimal.ZERO));

            if (studentFee.getDueAmount().compareTo(BigDecimal.ZERO) == 0)
                studentFee.setStatus(FeeStatus.PAID);

            else if (studentFee.getPaidAmount().compareTo(BigDecimal.ZERO) > 0)
                studentFee.setStatus(FeeStatus.PARTIAL);

            else
                studentFee.setStatus(FeeStatus.PENDING);


            studentFeeRepo.save(studentFee);



            FeePayment payment = FeePayment.builder()
                    .studentFee(studentFee)
                    .amount(paidAmount)
                    .paymentMethod(PaymentMethod.valueOf(req.getPaymentMode().toUpperCase()))
                    .status(PaymentStatus.SUCCESS)
                    .paymentDate(LocalDateTime.now())
                    .receivedById(req.getReceivedBy())
                    .razorpayOrderId(req.getRazorpayOrderId())
                    .razorpayPaymentId(req.getRazorpayPaymentId())
                    .razorpaySignature(req.getRazorpaySignature())
                    .build();


            feePaymentRepo.save(payment);



            Map<String,Object> pmap = new HashMap<>();
            pmap.put("id", payment.getId());
            pmap.put("fee_type", feeObj.getFeeType().name());
            pmap.put("amount", payment.getAmount().toPlainString());
            pmap.put("status", payment.getStatus().name());
            pmap.put("month", studentFee.getMonth());
            pmap.put("receipt_number", studentFee.getReceiptNumber());
            createdPayments.add(pmap);
        }


        return ConfirmPaymentResponse.builder()
                .message("Payment confirmed successfully.")
                .payments(createdPayments)
                .build();

    }


    @Override
    public Map<String, Object> getStudentUnpaidFees() {
        List<StudentFee> unpaid = studentFeeRepo.findByStatusIn(
                Arrays.asList(FeeStatus.PENDING, FeeStatus.PARTIAL)
        );

        Map<Long, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (StudentFee fee : unpaid) {
            Long studentYearId = fee.getStudentYearId();
            if (studentYearId == null) continue;

            // fetch student info from Django via Feign
            StudentYearLevelResponse studentYear = null;
            try {
                studentYear = studentYearClient.getStudentYearLevel(studentYearId);
            } catch (Exception e) {
                // swallow — we'll still surface record but with limited student info
            }

            Long studentId = studentYear != null ? studentYear.getStudentId() : null;
            String studentName = studentYear != null ? studentYear.getStudentName() : "N/A";
            String scholarNumber = studentYear != null ? studentYear.getScholarNumber() : "N/A";
            String yearLevelName = studentYear != null ? studentYear.getLevelName() : "N/A";

            // group by studentId (fallback to studentYearId if studentId null)
            Long groupKey = studentId != null ? studentId : studentYearId;

            grouped.computeIfAbsent(groupKey, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                Map<String,Object> stu = new LinkedHashMap<>();
                stu.put("id", studentId != null ? studentId : studentYearId);
                stu.put("name", studentName);
                stu.put("scholar_number", scholarNumber);
                m.put("student", stu);
                m.put("month", fee.getMonth() == null ? "Unknown" : java.time.Month.of(fee.getMonth()).name());
                m.put("school_year", fee.getSchoolYearId() != null ? fee.getSchoolYearId().toString() : "N/A");
                m.put("year_level_fees_grouped", new ArrayList<Map<String,Object>>());
                m.put("total_amount", BigDecimal.ZERO);
                m.put("paid_amount", BigDecimal.ZERO);
                m.put("due_amount", BigDecimal.ZERO);
                return m;
            });

            Map<String, Object> studentData = grouped.get(groupKey);
            List<Map<String,Object>> ylGroups = (List<Map<String,Object>>) studentData.get("year_level_fees_grouped");

            Map<String,Object> ylGroup = ylGroups.stream()
                    .filter(g -> yearLevelName.equals(g.get("year_level")))
                    .findFirst()
                    .orElseGet(() -> {
                        Map<String,Object> newg = new LinkedHashMap<>();
                        newg.put("year_level", yearLevelName);
                        newg.put("fees", new ArrayList<Map<String,Object>>());
                        ylGroups.add(newg);
                        return newg;
                    });

            List<Map<String,Object>> feesList = (List<Map<String,Object>>) ylGroup.get("fees");
            Map<String,Object> feeMap = new LinkedHashMap<>();
            feeMap.put("id", fee.getId());
            feeMap.put("fee_type", fee.getFeeStructure() != null ? fee.getFeeStructure().getFeeType().name() : "N/A");
            feeMap.put("original_amount", fee.getOriginalAmount() != null ? fee.getOriginalAmount().toPlainString() : "0.00");
            feesList.add(feeMap);

            // totals
            BigDecimal total = (BigDecimal) studentData.get("total_amount");
            BigDecimal paid = (BigDecimal) studentData.get("paid_amount");
            BigDecimal due = (BigDecimal) studentData.get("due_amount");
            total = total.add(fee.getOriginalAmount() != null ? fee.getOriginalAmount() : BigDecimal.ZERO);
            paid = paid.add(fee.getPaidAmount() != null ? fee.getPaidAmount() : BigDecimal.ZERO);
            due = due.add(fee.getDueAmount() != null ? fee.getDueAmount() : BigDecimal.ZERO);

            studentData.put("total_amount", total);
            studentData.put("paid_amount", paid);
            studentData.put("due_amount", due);
        }

        // convert BigDecimal totals to String
        for (Map<String,Object> s : grouped.values()) {
            s.put("total_amount", ((BigDecimal) s.get("total_amount")).toPlainString());
            s.put("paid_amount", ((BigDecimal) s.get("paid_amount")).toPlainString());
            s.put("due_amount", ((BigDecimal) s.get("due_amount")).toPlainString());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("unpaid_fees", new ArrayList<>(grouped.values()));
        return result;
    }













    @Override
    public List<Map<String,Object>> getOverdueFees(Long studentYearId, Integer month, Long schoolYearId) {

        LocalDate today = LocalDate.now();
        List<StudentFee> qry = studentFeeRepo
                .findByDueAmountGreaterThanAndDueDateBefore(BigDecimal.ZERO, today);

        // apply simple filters in-memory (ok if dataset small; for large dataset convert to repo queries)

        if (studentYearId != null) {
            qry = qry.stream()
                    .filter(f -> Objects.equals(f.getStudentYearId(), studentYearId))
                    .collect(Collectors.toList());
        }


        if (schoolYearId != null) {
            qry = qry.stream()
                    .filter(f -> Objects.equals(f.getSchoolYearId(), schoolYearId))
                    .collect(Collectors.toList());
        }



        if (month != null) {
            qry = qry.stream()
                    .filter(f -> f.getDueDate() != null && f.getDueDate().getMonthValue() == month)
                    .collect(Collectors.toList());
        }




        List<Map<String,Object>> response = new ArrayList<>();

        for (StudentFee fee : qry) {

            Map<String,Object> m = new LinkedHashMap<>();

            m.put("fee_id", fee.getId());

            m.put("fee_type", fee.getFeeStructure() != null
                    ? fee.getFeeStructure().getFeeType().name() : "N/A");

            m.put("original_amount", fee.getOriginalAmount() != null
                    ? fee.getOriginalAmount().toPlainString() : "0.00");

            m.put("paid_amount", fee.getPaidAmount() != null
                    ? fee.getPaidAmount().toPlainString() : "0.00");

            m.put("due_amount", fee.getDueAmount() != null
                    ? fee.getDueAmount().toPlainString() : "0.00");


            m.put("status", "Overdue");
            m.put("due_date", fee.getDueDate() != null
                    ? fee.getDueDate().toString() : null);


            // resolve student info via Feign (we only have studentYearId)
            StudentYearLevelResponse studentYear = null;

            try {
                studentYear = studentYearClient.getStudentYearLevel(fee.getStudentYearId());

            } catch (Exception e) {
                // ignore; still return record with limited info
            }


            String studentName = studentYear != null ? studentYear.getStudentName() : "N/A";
            String scholarNumber = studentYear != null ? studentYear.getScholarNumber() : "N/A";
            String className = studentYear != null ? studentYear.getLevelName() : "N/A";


            m.put("student_name", studentName);
            m.put("scholar_number", scholarNumber);
            m.put("class_name", className);
            m.put("month", fee.getDueDate() != null ? fee.getDueDate().getMonth().name() : null);


            response.add(m);

        }

        return response;
    }









    @Override
    public List<Map<String,Object>> getFeeHistory(Long studentYearId, Long schoolYearId) {

        List<StudentFee> studentFees = studentFeeRepo
                .findByStudentYearIdAndSchoolYearId(studentYearId, schoolYearId);

        List<Map<String,Object>> out = new ArrayList<>();

        for (StudentFee sf : studentFees) {

            Map<String,Object> m = new LinkedHashMap<>();

            m.put("fee_type", sf.getFeeStructure() != null
                    ? sf.getFeeStructure().getFeeType().name() : "N/A");

            m.put("original_amount", sf.getOriginalAmount() != null
                    ? sf.getOriginalAmount().toPlainString() : "0.00");

            m.put("paid_amount", sf.getPaidAmount() != null
                    ? sf.getPaidAmount().toPlainString() : "0.00");


            m.put("due_amount", sf.getDueAmount() != null
                    ? sf.getDueAmount().toPlainString() : "0.00");

            m.put("status", sf.getStatus() != null
                    ? sf.getStatus().name() : "N/A");


            m.put("month", sf.getMonth() != null
                    ? java.time.Month.of(sf.getMonth()).name() : null);


            // payments via repo
            List<FeePayment> payments = feePaymentRepo.findByStudentFee(sf.getId());

            List<Map<String,Object>> paymentsOut = payments.stream().map(p -> {

                Map<String,Object> pm = new HashMap<>();
                pm.put("amount", p.getAmount().toPlainString());
                pm.put("method", p.getPaymentMethod());
                pm.put("status", p.getStatus());
                pm.put("date", p.getPaymentDate());
                return pm;
            }).collect(Collectors.toList());

            m.put("payments", paymentsOut);



            out.add(m);

        }

        return out;

    }




    @Override
    public List<Map<String,Object>> getPendingFees(Long schoolYearId) {

        if (schoolYearId == null) {
            throw new ApiException("school_year_id is required", HttpStatus.BAD_REQUEST);

        }


        List<StudentFee> pending = studentFeeRepo.
                findBySchoolYearIdAndStatusNot(schoolYearId, FeeStatus.PAID);


        if (pending == null || pending.isEmpty())
            return Collections.emptyList();


        List<Map<String,Object>> out = new ArrayList<>();

        for (StudentFee fee : pending) {
            AppliedFeeDiscount d = discountRepo
                    .findFirstByStudentYearIdAndFeeStructure(fee.getStudentYearId(), fee.getFeeStructure())
                    .orElse(null);

            BigDecimal discountAmount = d != null ? d.getDiscountAmount() : BigDecimal.ZERO;

            BigDecimal adjustedOriginal = fee.getOriginalAmount() != null
                    ? fee.getOriginalAmount().subtract(discountAmount).max(BigDecimal.ZERO) : BigDecimal.ZERO;



            Map<String,Object> m = new LinkedHashMap<>();

            m.put("fee_id", fee.getId());
            m.put("fee_type", fee.getFeeStructure() != null
                    ? fee.getFeeStructure().getFeeType().name() : "N/A");

            m.put("original_amount", adjustedOriginal.toPlainString());

            m.put("paid_amount", fee.getPaidAmount() != null
                    ? fee.getPaidAmount().toPlainString() : "0.00");

            m.put("status", fee.getStatus() != null
                    ? fee.getStatus().name() : "N/A");

            out.add(m);

        }

        return out;

    }












//    // Secure random receipt generator (10 chars uppercase+digits) - DB unique constraint will protect collisions
//    private String generateReceipt() {
//        final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//        var rnd = new java.security.SecureRandom();
//        StringBuilder sb = new StringBuilder(10);
//        for (int i = 0; i < 10; i++) sb.append(ALPHANUM.charAt(rnd.nextInt(ALPHANUM.length())));
//        return sb.toString();
//    }





    private Long resolveYearLevelId(StudentYearLevelResponse studentYear) {

        // If server later returns level_id directly, use it
        // if (studentYear.getLevelId() != null) return studentYear.getLevelId();

        String levelName = studentYear.getLevelName();
        if (levelName == null || levelName.isBlank()) {
            throw new ApiException("StudentYearLevel is missing 'level_name' field.",
                    HttpStatus.BAD_GATEWAY);
        }

        List<YearLevelResponse> levels;
        try {
            levels = yearLevelClient.getAllYearLevels();
        } catch (Exception ex) {
            throw new ApiException("Failed to fetch YearLevels from Django: "
                    + ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }

        final String target = levelName.trim().toLowerCase();

        return levels.stream()
                .filter(l -> l.getLevelName() != null &&
                        l.getLevelName().trim().toLowerCase().equals(target))
                .findFirst()
                .map(YearLevelResponse::getId)
                .orElseThrow(() ->
                        new ApiException("YearLevel '" + levelName + "' not found in Django.",
                                HttpStatus.NOT_FOUND)
                );
    }




    private String generateReceipt() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "REC-" + today + "-";
        // find highest existing that starts with prefix
        Optional<StudentFee> top = studentFeeRepo.findTopByReceiptNumberStartingWithOrderByReceiptNumberDesc(prefix);
        int nextNo = 1;
        if (top.isPresent()) {
            String last = top.get().getReceiptNumber(); // e.g. REC-20251201-00012-ABCD
            // parse middle numeric part
            String[] parts = last.split("-");
            if (parts.length >= 3) {
                try {
                    nextNo = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        String uniqueSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        String receipt = String.format("REC-%s-%05d-%s", today, nextNo, uniqueSuffix);
        // ensure unique (small loop just in case)
        while (studentFeeRepo.existsByReceiptNumber(receipt)) {
            nextNo++;
            uniqueSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
            receipt = String.format("REC-%s-%05d-%s", today, nextNo, uniqueSuffix);
        }
        return receipt;
    }




}
