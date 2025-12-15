package com.java.sms.service;



import com.java.sms.DataClass.MasterFeeRequest;
import com.java.sms.response.MasterFeeResponse;

import java.util.List;

public interface MasterFeeService {

    MasterFeeResponse create(MasterFeeRequest req);

    MasterFeeResponse update(Long id, MasterFeeRequest req);

    MasterFeeResponse findById(Long id);

    List<MasterFeeResponse> findAll();

    void delete(Long id);


}

