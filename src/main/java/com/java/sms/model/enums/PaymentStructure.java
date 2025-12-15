package com.java.sms.model.enums;



public enum PaymentStructure {
    MONTHLY,
    QUARTERLY,
    YEARLY,
    OTHERS;


    public static PaymentStructure fromString(String s) {
        return switch (s == null ? "" : s.toLowerCase()) {
            case "monthly" -> MONTHLY;
            case "quarterly" -> QUARTERLY;
            case "yearly" -> YEARLY;
            case "others" -> OTHERS;
            default -> throw new IllegalArgumentException("Unknown payment structure: " + s);
        };
    }


}
