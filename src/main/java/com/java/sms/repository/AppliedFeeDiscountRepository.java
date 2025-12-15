package com.java.sms.repository;


import com.java.sms.model.AppliedFeeDiscount;
import com.java.sms.model.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Repository for AppliedFeeDiscount.
 *
 * NOTE:
 * - We expose a findFirstBy... method (derived query) and
 * - a proper @Query for sum of discount_amount so Spring won't try to derive
 *   a complex "sum..." property path (which caused your startup error).
 *
 * Make sure the entity field names used in the JPQL below (studentYearId, feeStructure)
 * match the actual field names in your AppliedFeeDiscount entity.
 * If your entity uses a different property (e.g. "student" referencing a StudentYear entity),
 * update the JPQL accordingly, e.g. "a.student.id = :studentYearId".
 */
public interface AppliedFeeDiscountRepository extends JpaRepository<AppliedFeeDiscount, Long> {

    /**
     * Find a single discount record for a student-year + fee structure (first if multiple).
     * This mirrors the Django `AppliedFeeDiscount.objects.filter(...).first()` usage.
     */
    Optional<AppliedFeeDiscount> findFirstByStudentYearIdAndFeeStructure(Long studentYearId, FeeStructure feeStructure);

    /**
     * Sum of discount_amount for given studentYearId and feeStructure.
     *
     * IMPORTANT: adjust the `a.studentYearId` part if your entity uses a different property.
     * For example, if your entity has `private StudentYear student;` then use `a.student.id = :studentYearId`.
     */
//    @Query("SELECT COALESCE(SUM(a.discountAmount), 0) FROM AppliedFeeDiscount a " +
//            "WHERE a.studentYearId = :studentYearId AND a.feeStructure = :feeStructure")
//    BigDecimal sumDiscountByStudentYearIdAndFeeStructure(@Param("studentYearId") Long studentYearId,
//                                                         @Param("feeStructure") FeeStructure feeStructure);



    @Query("SELECT COALESCE(SUM(d.discountAmount), 0) FROM AppliedFeeDiscount d " +
            "WHERE d.studentYearId = :studentYearId AND d.feeStructure = :feeStructure")
    BigDecimal sumDiscountAmountByStudentYearIdAndFeeStructure(
            @Param("studentYearId") Long studentYearId,
            @Param("feeStructure") FeeStructure feeStructure);



    List<AppliedFeeDiscount> findByStudentYearId(Long studentId);

//    Optional<AppliedFeeDiscount> findFirstByStudentYearIdAndFeeStructure(Long studentId, FeeStructure feeType);

    boolean existsByStudentYearIdAndFeeStructure(Long studentId, FeeStructure feeType);


}




