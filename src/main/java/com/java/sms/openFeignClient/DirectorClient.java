package com.java.sms.openFeignClient;

import com.java.sms.DataClass.DirectorDTO;
import com.java.sms.config.FeignConfig;
import com.java.sms.response.DirectorsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "director-service", url = "${external.django.base-url}",
        configuration = FeignConfig.class)
public interface DirectorClient {

    @PostMapping("/d/director/")
    DirectorDTO createDirector(@RequestBody DirectorDTO directorDTO);

    @GetMapping("/d/director/by-user/{user_id}/")  // Spring calls this
    DirectorsResponse getDirectorById(@PathVariable("user_id") Long id);

    @GetMapping(
            value = "/d/director/",
            produces = "application/json"
    )
    DirectorsResponse getAllDirectors();
}

