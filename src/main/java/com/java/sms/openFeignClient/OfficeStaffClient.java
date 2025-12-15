package com.java.sms.openFeignClient;

import com.java.sms.DataClass.OfficeStaffDTO;
import com.java.sms.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "office-staff-service", url = "${external.django.base-url}",
        configuration = FeignConfig.class)
public interface OfficeStaffClient {

    @PostMapping("/d/officestaff/")
    OfficeStaffDTO createOfficeStaff(@RequestBody OfficeStaffDTO officeStaffDTO);

    @GetMapping("/d/officestaff/{id}/")
    OfficeStaffDTO getOfficeStaffById(@PathVariable("id") Long id);
}


