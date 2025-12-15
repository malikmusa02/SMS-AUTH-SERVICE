package com.java.sms.openFeignClient;


import com.java.sms.DataClass.GuardianDTO;
import com.java.sms.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "Guardian-service", url = "${external.django.base-url}",
        configuration = FeignConfig.class)
public interface GuardianClient {

    @PostMapping("/g/guardian/")
    GuardianDTO creteGuardian(@RequestBody GuardianDTO guardianDTO);


}
