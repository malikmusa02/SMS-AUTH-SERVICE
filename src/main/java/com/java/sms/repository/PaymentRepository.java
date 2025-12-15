package com.java.sms.repository;


import com.java.sms.model.Payment;
import com.java.sms.model.enums.PaymentMethod;
import com.java.sms.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByPaymentMethod(PaymentMethod method, Pageable pageable);

    // combined filter
    Page<Payment> findByStatusAndPaymentMethod(PaymentStatus status, PaymentMethod method, Pageable pageable);

    boolean existsByChequeNumber(String chequeNumber);

}
