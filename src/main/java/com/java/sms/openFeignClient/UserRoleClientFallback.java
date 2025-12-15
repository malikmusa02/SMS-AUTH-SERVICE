package com.java.sms.openFeignClient;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Fallback implementation of {@link UserRoleClient}.
 *
 * <p>If Django service is down, unreachable, or responds with an error,
 * Feign automatically switches to this fallback.
 *
 * <p>We return an empty map so authentication flow is NOT broken.
 */
@Component
public class UserRoleClientFallback implements UserRoleClient {

    @Override
    public Map<String, Object> getUserRoleId(Long userId) {
        // Django down? Return empty map, let AuthService continue login.
        return Collections.emptyMap();
    }
}
