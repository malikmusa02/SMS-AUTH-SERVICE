package com.java.sms.repository;


import com.java.sms.model.MasterFee;
import com.java.sms.model.enums.PaymentStructure;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterFeeRepository extends JpaRepository<MasterFee, Long> {

}

