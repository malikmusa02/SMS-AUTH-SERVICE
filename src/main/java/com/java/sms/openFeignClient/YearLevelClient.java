package com.java.sms.openFeignClient;



import com.java.sms.DataClass.AssignRequest;
import com.java.sms.config.FeignConfig;
import com.java.sms.response.YearLevelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "Django-service",
        url = "${external.django.base-url}",
        configuration = FeignConfig.class)
public interface YearLevelClient {

    /**
     * Forward assign-to-yearlevel request to Django.
     * Adjust the path if your Django endpoint path differs.
     */
    @PostMapping("/d/classperiods/assign-to-yearlevel/")
    Map<String, Object> assignToYearLevel(@RequestBody AssignRequest request);




    @GetMapping("/d/year-level/{id}/")
    YearLevelResponse getYearLevelById(@PathVariable("id") Long id);



    // list endpoint (Django typically provides a list at /d/year-level/).
    // If your Django path differs adjust it.

    @GetMapping("/d/year-levels/")
    List<YearLevelResponse> getAllYearLevels();



}



