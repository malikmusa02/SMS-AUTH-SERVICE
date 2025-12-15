package com.java.sms.openFeignClient;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.Map;



/**
 * Feign client used to communicate with the Django microservice
 * that returns role-specific IDs for a user.
 *
 * <p>Django endpoint: GET /api/get-user-role-id/{userId}
 *
 * <p>Examples of possible Django responses:
 * <pre>
 *     {"StudentId": 12}
 *     {"TeacherId": 55}
 *     {"OfficeStaff.Id": 7}
 *     {"DirectorId": 99}
 * </pre>
 *
 * <p>Because Django may return unpredictable key formats,
 * we keep this client response flexible by returning Map<String, Object>.
 *
 * <p>The normalization and mapping are handled in {@code RoleServiceForDjango}.
 */
@FeignClient(
        name = "django-role-service",
        url = "${external.django.base-url}",
        fallback = UserRoleClientFallback.class  // ensures login doesn't fail if Django is offline
)
public interface UserRoleClient {


    /**
     * Fetches raw role data for a user from the Django server.
     *
     * @param userId User ID to check.
     * @return A map containing one key (e.g., StudentId -> value), or empty map if not found.
     */
    @GetMapping("/t/user-role/{userId}/")
    Map<String, Object> getUserRoleId(@PathVariable("userId") Long userId);


}