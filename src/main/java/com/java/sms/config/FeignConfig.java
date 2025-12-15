package com.java.sms.config;

import com.java.sms.security.JwtTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
public class FeignConfig implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        // If no request context (startup stage), skip token
        if (RequestContextHolder.getRequestAttributes() == null) {
            System.out.println("Startup detected → Skipping JWT forwarding.");
            return;
        }

        String token = JwtTokenProvider.getCurrentToken();

        if (token != null) {
            template.header("Authorization", "Bearer " + token);
            System.out.println("Forwarded JWT token to Django: " + token);
        } else {
            System.out.println("No JWT token found → Skipping for non-HTTP calls.");
        }
    }
}
