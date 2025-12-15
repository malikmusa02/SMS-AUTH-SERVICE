package com.java.sms.openFeignClient;


import com.java.sms.DataClass.StudentDTO;
import com.java.sms.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "Student-service", url = "${external.django.base-url}",
        configuration = FeignConfig.class)
public interface StudentClient {


    @PostMapping("/s/student/")
    StudentDTO createStudent(@RequestBody StudentDTO studentDTO);

    @GetMapping("/student/get/{id}")
    StudentDTO getStudentById(@PathVariable("id") Long id);

}
