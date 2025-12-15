package com.java.sms.openFeignClient;


import com.java.sms.response.SchoolYearResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// SchoolYear fetch
@FeignClient(name = "django-school-year", url = "${external.django.base-url}",
        configuration = com.java.sms.config.FeignJacksonConfig.class,
        fallbackFactory = SchoolYearClientFallbackFactory.class)
public interface SchoolYearClient {

    @GetMapping("/d/school-year/{id}/")
    SchoolYearResponse getSchoolYear(@PathVariable("id") Long id);

}

