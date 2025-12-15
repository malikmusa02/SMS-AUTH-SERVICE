package com.java.sms.openFeignClient;


import com.java.sms.exception.ApiException;
import com.java.sms.response.StudentYearLevelResponse;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * FallbackFactory for StudentYearClient.
 * Triggered if Django service is DOWN, returns 5xx, or Feign decoding fails.
 */
@Component
public class StudentYearClientFallbackFactory implements FallbackFactory<StudentYearClient> {

    private static final Logger log = LoggerFactory.getLogger(StudentYearClientFallbackFactory.class);

    @Override
    public StudentYearClient create(Throwable cause) {

        return new StudentYearClient() {

            @Override
            public StudentYearLevelResponse getStudentYearLevel(Long id) {
                log.error("Fallback triggered for StudentYearClient.getStudentYearLevel(id={}) - cause={}",
                        id, cause == null ? "unknown" : cause.getMessage(), cause);

                //  Option A: Soft fallback (return null, service layer handles error)
                return null;

                // Option B: Strict fallback — Uncomment to throw exception instead
                // throw new ApiException("StudentYear service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
            }
        };
    }
}

