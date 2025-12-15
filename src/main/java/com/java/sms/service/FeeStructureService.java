package com.java.sms.service;



import com.java.sms.DataClass.FeeStructureRequest;
import com.java.sms.response.FeeStructureResponse;

import java.util.List;

public interface FeeStructureService {

    FeeStructureResponse create(FeeStructureRequest request);

    FeeStructureResponse update(Long id, FeeStructureRequest request);

    FeeStructureResponse findById(Long id);

    List<FeeStructureResponse> findAll();

    List<FeeStructureResponse> findByYearLevelId(Long yearLevelId);

    void delete(Long id);


}

