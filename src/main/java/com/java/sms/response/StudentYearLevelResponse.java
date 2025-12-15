package com.java.sms.response;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;



/**
 * Maps the exact JSON returned by Django for student-year endpoint:
 * {
 *   "id": 4,
 *   "student_id": 6,
 *   "student_name": "Unknown Student",
 *   "student_email": "",
 *   "scholar_number": null,
 *   "level_name": "Class 1",
 *   "year_name": "2025-2026"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentYearLevelResponse {
    private Long id;

    @JsonProperty("student_id")
    private Long studentId;

    @JsonProperty("student_name")
    private String studentName;

    @JsonProperty("student_email")
    private String studentEmail;

    @JsonProperty("scholar_number")
    private String scholarNumber;


    // NOTE: Django returns only the name here (no nested object)
    @JsonProperty("level_name")
    private String levelName;

    @JsonProperty("year_name")
    private String yearName;


    // Optional: if your Django later returns nested 'level' with id, keep this for backward compat
    // private LevelInfo level;


    // convenience safe getters (optional)

    public String safeStudentName() {
        return studentName != null ? studentName : "Unknown Student";
    }

    public String safeLevelName() {
        return levelName != null ? levelName : "Unknown";
    }

    public String safeYearName() {
        return yearName != null ? yearName : "Unknown";
    }



}








