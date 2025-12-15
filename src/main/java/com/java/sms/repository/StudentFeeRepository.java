package com.java.sms.repository;


import com.java.sms.model.FeeStructure;
import com.java.sms.model.StudentFee;
import com.java.sms.model.enums.FeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;



public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {
    List<StudentFee> findByStudentYearId(Long studentYearId);

    Optional<StudentFee> findByStudentYearIdAndFeeStructureIdAndMonthAndSchoolYearId(
            Long studentYearId, Long feeStructureId, Integer month, Long schoolYearId);

    List<StudentFee> findByStudentYearIdAndFeeStructureId(Long studentYearId, Long feeStructureId);





    Optional<StudentFee> findTopByReceiptNumberStartingWithOrderByReceiptNumberDesc(String prefix);

    boolean existsByReceiptNumber(String receiptNumber);

    List<StudentFee> findByStatusIn(List<FeeStatus> statuses);

    List<StudentFee> findByDueAmountGreaterThanAndDueDateBefore(BigDecimal amount, LocalDate date);

    List<StudentFee> findByStudentYearIdAndSchoolYearId(Long studentYearId, Long schoolYearId);

    List<StudentFee> findBySchoolYearIdAndStatusNot(Long schoolYearId, FeeStatus status);




    List<StudentFee> findByStudentYearIdAndStatusIn(
            Long studentYearId,
            List<FeeStatus> statuses
    );

}


