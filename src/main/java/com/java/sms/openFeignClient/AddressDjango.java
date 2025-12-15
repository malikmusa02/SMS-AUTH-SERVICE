package com.java.sms.openFeignClient;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "addressService", url = "${external.django.base-url}")
public interface AddressDjango {
}
