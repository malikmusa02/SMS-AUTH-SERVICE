package com.java.sms.DataClass;


import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds normalized role-specific entity IDs for a given user.
 *
 * <p>This class is used to store IDs returned from the Django microservice,
 * which checks multiple models (Student, Teacher, Guardian, OfficeStaff, Director).
 *
 * <p>Django may return keys in different formats such as:
 * <ul>
 *     <li>"StudentId"</li>
 *     <li>"TeacherId"</li>
 *     <li>"OfficeStaff.Id"</li>
 *     <li>"GuardianId"</li>
 *     <li>"DirectorId"</li>
 * </ul>
 *
 * <p>After normalization, they are mapped into the fields of this class.
 *
 * <p>The {@code toMap()} method converts the structure into a clean
 * ROLE → roleId map, ready to be included in API responses.
 */
@Getter
@Setter
public class RoleEntities {

    /** Student table ID for this user (if exists). */
    private Long studentId;

    /** Teacher table ID for this user (if exists). */
    private Long teacherId;

    /** Guardian table ID for this user (if exists). */
    private Long guardianId;

    /** Office Staff table ID for this user (if exists). */
    private Long officeStaffId;

    /** Director table ID for this user (if exists). */
    private Long directorId;



    /**
     * Checks if no role IDs are set.
     *
     * @return true if this user has no recognized role ID.
     */
    public boolean isEmpty() {
        return studentId == null &&
                teacherId == null &&
                guardianId == null &&
                officeStaffId == null &&
                directorId == null;
    }

    /**
     * Converts the role IDs into clean JSON-friendly key names:
     * studentId, teacherId, guardianId, officeStaffId, directorId
     */
    public Map<String, Long> toMap() {
        Map<String, Long> m = new HashMap<>();

        if (studentId != null) m.put("studentId", studentId);
        if (teacherId != null) m.put("teacherId", teacherId);
        if (guardianId != null) m.put("guardianId", guardianId);
        if (officeStaffId != null) m.put("officeStaffId", officeStaffId);
        if (directorId != null) m.put("directorId", directorId);

        return m;
    }


    @Override
    public String toString() {
        return "RoleEntities{" +
                "studentId=" + studentId +
                ", teacherId=" + teacherId +
                ", guardianId=" + guardianId +
                ", officeStaffId=" + officeStaffId +
                ", directorId=" + directorId +
                '}';
    }
}
