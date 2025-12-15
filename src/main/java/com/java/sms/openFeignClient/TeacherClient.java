package com.java.sms.openFeignClient;

import com.java.sms.DataClass.TeacherDTO;
import com.java.sms.config.FeignConfig;
import com.java.sms.response.TeacherResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "teacher-service", url = "${external.django.base-url}",
        configuration = FeignConfig.class)
public interface TeacherClient {

    @PostMapping("/t/teacher/")
    TeacherResponseDTO createTeacher(@RequestBody TeacherDTO teacherDTO);

    @GetMapping("/teachers/get/{id}")
    TeacherDTO getTeacherById(@PathVariable("id") Long id);
}

