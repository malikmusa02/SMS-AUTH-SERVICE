package com.java.sms.config;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Utility to convert stacktrace to string. */
public final class ExceptionUtils {
    private ExceptionUtils() {}

    public static String stackTraceAsString(Throwable t) {
        if (t == null) return "";
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
