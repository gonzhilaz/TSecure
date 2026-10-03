/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import java.util.Objects;

public final class CompareUtils {
    private CompareUtils() {
    }

    public static boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }
}
