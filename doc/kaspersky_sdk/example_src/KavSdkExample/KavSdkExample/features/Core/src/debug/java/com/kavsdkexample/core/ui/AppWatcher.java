/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.ui;

import android.app.Activity;
import androidx.fragment.app.Fragment;

public final class AppWatcher {
    public static void watch(Activity activity) {
        //leakcanary.AppWatcher.INSTANCE.getObjectWatcher().watch(activity);
    }

    public static void watch(Fragment fragment) {
        //leakcanary.AppWatcher.INSTANCE.getObjectWatcher().watch(fragment);
    }
}