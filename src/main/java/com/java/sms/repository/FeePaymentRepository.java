package com.java.sms.repository;

import com.java.sms.model.FeePayment;
import com.java.sms.model.StudentFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {


    // FeePaymentRepository
    @Query("select f from FeePayment f where f.studentFee.id = :studentFeeId")
    List<FeePayment> findByStudentFee(@Param("studentFeeId") Long studentFeeId);



//    COALESCE prevents NULL issues
    @Query("select coalesce(sum(f.amount), 0) from FeePayment f where f.studentFee.id = :studentFeeId")
    BigDecimal sumAmountByStudentFeeId(@Param("studentFeeId") Long studentFeeId);

}
