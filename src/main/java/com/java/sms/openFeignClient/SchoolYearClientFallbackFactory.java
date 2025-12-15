package com.java.sms.openFeignClient;


import com.java.sms.exception.ApiException;
import com.java.sms.response.SchoolYearResponse;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * FallbackFactory for SchoolYearClient.
 */
@Component
public class SchoolYearClientFallbackFactory implements FallbackFactory<SchoolYearClient> {

    private static final Logger log = LoggerFactory.getLogger(SchoolYearClientFallbackFactory.class);

    @Override
    public SchoolYearClient create(Throwable cause) {

        return new SchoolYearClient() {

            @Override
            public SchoolYearResponse getSchoolYear(Long id) {
                log.error("Fallback triggered for SchoolYearClient.getSchoolYear(id={}) - cause={}",
                        id, cause == null ? "unknown" : cause.getMessage(), cause);

                // Option A: Soft fallback (return null)
                return null;

                //  Option B: Strict fallback — Uncomment if you prefer failing fast
                // throw new ApiException("SchoolYear service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);
            }
        };
    }
}
