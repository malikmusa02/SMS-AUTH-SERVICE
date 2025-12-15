package com.java.sms.serviceImpl;

import com.java.sms.DataClass.RoleEntities;
import com.java.sms.openFeignClient.UserRoleClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

/**
 * Service responsible for fetching role-specific entity IDs
 * (StudentId, TeacherId, GuardianId, OfficeStaffId, DirectorId)
 * from the Django microservice using a Feign client.
 *
 * <p>This class normalizes Django response keys such as:
 * <ul>
 *     <li>"StudentId"</li>
 *     <li>"student_id"</li>
 *     <li>"OfficeStaff.Id"</li>
 *     <li>"DirectorId"</li>
 * </ul>
 *
 * and maps them into a unified {@link RoleEntities} object.
 *
 * <p>This ensures the Spring Boot application remains stable even when
 * Django returns inconsistent or mixed naming conventions.
 *
 * <p>NOTE: The Django API always returns only one role per user,
 * matching the first found entity in their order of checks.
 */
@Service
public class RoleServiceForDjango {

    private final Logger log = LoggerFactory.getLogger(RoleServiceForDjango.class);
    private final UserRoleClient userRoleClient;

    public RoleServiceForDjango(UserRoleClient userRoleClient) {
        this.userRoleClient = userRoleClient;
    }

    /**
     * Fetches role-specific entity IDs for the given user by calling the Django API.
     *
     * <p>Example Django API responses:
     * <pre>
     *     {"StudentId": 12}
     *     {"TeacherId": 55}
     *     {"OfficeStaff.Id": 7}
     *     {"DirectorId": 99}
     *     {"GuardianId": 33}
     * </pre>
     *
     * <p>All keys are normalized into a clean form (e.g., "OFFICE_STAFF"),
     * and mapped into {@link RoleEntities}.
     *
     * @param userId The ID of the user whose role-related IDs need to be fetched.
     * @return A populated {@link RoleEntities} object containing available role IDs.
     *         If Django returns nothing or an error occurs, returns empty RoleEntities.
     */
    public RoleEntities fetchRoleEntitiesForUser(Long userId) {

        RoleEntities re = new RoleEntities();

        try {
            // Call Django microservice
            Map<String, Object> raw = userRoleClient.getUserRoleId(userId);

            // If nothing returned, log and return empty object
            if (raw == null || raw.isEmpty()) {
                log.debug("No role info returned for user {}", userId);
                return re;
            }

            // Iterate through each returned field (usually only 1 key)
            raw.forEach((rawKey, rawValue) -> {
                if (rawValue == null) return;

                // Attempt to convert to Long safely
                Long id = parseLongSafely(rawValue);
                if (id == null) return;

                // Normalize messy Django key formats into clean identifiers
                String normalized = normalizeRoleKey(rawKey);

                // Switch on normalized role name
                switch (normalized) {
                    case "STUDENT" -> re.setStudentId(id);
                    case "TEACHER" -> re.setTeacherId(id);
                    case "GUARDIAN" -> re.setGuardianId(id);
                    case "OFFICE_STAFF", "OFFICESTAFF" -> re.setOfficeStaffId(id);
                    case "DIRECTOR" -> re.setDirectorId(id);
                    default -> {
                        // Extra safety fallback for unexpected formats
                        if (normalized.contains("STUDENT")) re.setStudentId(id);
                        else if (normalized.contains("TEACHER")) re.setTeacherId(id);
                        else if (normalized.contains("GUARDIAN")) re.setGuardianId(id);
                        else if (normalized.contains("OFFICE") || normalized.contains("STAFF")) re.setOfficeStaffId(id);
                        else if (normalized.contains("DIRECTOR")) re.setDirectorId(id);
                        else log.debug("Unknown role key from Django: {} -> {}", rawKey, normalized);
                    }
                }
            });

        } catch (Exception ex) {
            // We do NOT break login flow—just log error
            log.error("Error fetching role entities for user {}: {}", userId, ex.getMessage());
        }

        return re;
    }

    /**
     * Safely parses any object returned by Django into a Long.
     * Handles cases like:
     * <ul>
     *   <li>Integer</li>
     *   <li>Long</li>
     *   <li>String "12"</li>
     * </ul>
     *
     * @param v The value to parse
     * @return Parsed Long, or null if parsing fails
     */
    private Long parseLongSafely(Object v) {
        try {
            if (v instanceof Number) {
                return ((Number) v).longValue();
            }
            return Long.parseLong(v.toString());
        } catch (Exception e) {
            log.warn("Unable to parse role id value: {}", v);
            return null;
        }
    }

    /**
     * Normalizes Django response keys into clean uppercase identifiers.
     *
     * <p>Examples:
     * <pre>
     * "StudentId"       -> "STUDENT"
     * "student_id"      -> "STUDENT"
     * "OfficeStaff.Id"  -> "OFFICE_STAFF"
     * "DirectorId"      -> "DIRECTOR"
     * </pre>
     *
     * @param rawKey The original key from Django
     * @return A normalized role name (e.g., "STUDENT", "OFFICE_STAFF")
     */
    private String normalizeRoleKey(String rawKey) {
        if (rawKey == null) return "UNKNOWN";

        String k = rawKey.trim();

        // Replace dots and spaces with underscore
        k = k.replaceAll("[\\.\\s]+", "_");

        // Convert camelCase to snake_case ("StudentId" -> "Student_Id")
        k = k.replaceAll("([a-z])([A-Z])", "$1_$2");

        // Remove trailing "id" or "_id"
        k = k.replaceAll("(?i)(_?id)$", "");

        // Normalize multiple underscores and convert to uppercase
        k = k.replaceAll("_+", "_").toUpperCase(Locale.ROOT);

        return k;
    }
}
