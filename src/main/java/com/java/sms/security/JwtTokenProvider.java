package com.java.sms.security;



import org.springframework.stereotype.Component;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class JwtTokenProvider {

    public static String getCurrentToken() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) return null;

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7); // remove "Bearer "
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

