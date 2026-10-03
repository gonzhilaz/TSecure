/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static String stackTraceToString(Throwable e) {
        if (e == null) {
            return "";
        }

        final StringWriter result = new StringWriter();
        try {
            e.printStackTrace(new PrintWriter(result));

        } catch (IllegalAccessError error) { // https://issuetracker.google.com/71056660
        }
        return result.toString();
    }
}
