package com.java.sms.openFeignClient;


import com.java.sms.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(
        name = "Django-Lookup-Service",
        url = "${external.django.base-url}",
        configuration = FeignConfig.class
)
public interface DjangoLookupClient {

    @GetMapping(value = "/d/subject/{id}/", produces = "application/json")
    Map<String, Object> getSubjectById(@PathVariable("id") Long id);

    @GetMapping(value = "/d/year-level/{id}/", produces = "application/json")
    Map<String, Object> getYearLevelById(@PathVariable("id") Long id);

    @GetMapping(value = "/t/teacher/{id}/", produces = "application/json")
    Map<String, Object> getTeacherById(@PathVariable("id") Long id);

    @GetMapping(value = "/d/terms/{id}/", produces = "application/json")
    Map<String, Object> getTermById(@PathVariable("id") Long id);
}
