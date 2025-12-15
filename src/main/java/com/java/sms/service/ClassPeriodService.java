package com.java.sms.service;

import com.java.sms.DataClass.AssignRequest;
import com.java.sms.DataClass.ClassPeriodRequest;
import com.java.sms.model.ClassPeriod;

import java.util.List;
import java.util.Map;

public interface ClassPeriodService {

   ClassPeriod createClassPeriod(ClassPeriodRequest req);

   ClassPeriod getClassPeriodById(Long id);

   List<ClassPeriod> getAllClassPeriods();

   ClassPeriod updateClassPeriod(Long id, ClassPeriodRequest req);

   void deleteClassPeriod(Long id);

//    public Map<String, Object> assignToYearLevel(AssignRequest request);
}
