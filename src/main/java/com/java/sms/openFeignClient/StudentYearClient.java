package com.java.sms.openFeignClient;


import com.java.sms.response.StudentYearLevelResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// StudentYearLevel fetch (Django)
@FeignClient(name = "django-student-year", url = "${external.django.base-url}",
        configuration = com.java.sms.config.FeignJacksonConfig.class,
        fallbackFactory = StudentYearClientFallbackFactory.class)
public interface StudentYearClient {


    @GetMapping("/s/studentyearlevels/{id}/")
    StudentYearLevelResponse getStudentYearLevel(@PathVariable("id") Long id);



}
