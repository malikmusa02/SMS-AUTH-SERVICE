package com.java.sms.model.enums;

public enum FeeType {
    ADMISSION_FEE,
    EXAM_FEE,
    TUITION_FEE,
    CAUTION_FEE,
    MAINTENANCE,
    FORM_FEE,
    OTHERS;

    public static FeeType fromString(String s) {
        return switch (s == null ? "" : s.toLowerCase()) {
            case "admission fee" -> ADMISSION_FEE;
            case "exam fee" -> EXAM_FEE;
            case "tuition fee" -> TUITION_FEE;
            case "caution fee" -> CAUTION_FEE;
            case "maintenance" -> MAINTENANCE;
            case "form fee" -> FORM_FEE;
            case "others" -> OTHERS;
            default -> throw new IllegalArgumentException("Unknown fee type: " + s);
        };
    }
}

