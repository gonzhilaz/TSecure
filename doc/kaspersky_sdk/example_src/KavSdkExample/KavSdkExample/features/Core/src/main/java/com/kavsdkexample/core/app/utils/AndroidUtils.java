/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.Iterator;
import java.util.List;

public final class AndroidUtils {
    private static final String SYSTEM_FOLDER_PATH = "/system";
    private AndroidUtils() {
    }

    public static List<ApplicationInfo> getApplications(Context context, boolean excludeSystem, boolean excludeSelf) {
        PackageManager pm = context.getPackageManager();

        ApplicationInfo thisApp;
        try {
            thisApp = pm.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("Can't obtain package info for current application", e);
        }

        List<ApplicationInfo> installedApps = pm.getInstalledApplications(0);
        Iterator<ApplicationInfo> iter = installedApps.iterator();

        while (iter.hasNext()) {
            ApplicationInfo currentApp = iter.next();

            // We do not scan our application, systems services and system applications
            if (excludeSelf && currentApp.packageName.equals(thisApp.packageName) ||
                excludeSystem && (currentApp.uid == 0) || currentApp.sourceDir.startsWith(SYSTEM_FOLDER_PATH)) {
                iter.remove();
            }
        }
        return installedApps;
    }

    @Nullable
    public static Fragment findFragmentByTag(@NonNull FragmentActivity activity, @NonNull String tag) {
        return findFragmentByTag(activity.getSupportFragmentManager(), tag);
    }

    private static Fragment findFragmentByTag(@NonNull FragmentManager fragmentManager, @NonNull String tag) {
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (tag.equals(fragment.getTag())) {
                return fragment;
            }
            Fragment innerFragment = findFragmentByTag(fragment.getChildFragmentManager(), tag);
            if (innerFragment != null) {
                return innerFragment;
            }
        }
        return null;
    }

    public static <T extends Object> T newInstance(Class<? extends T> clazz) {
        try {
            return (T) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
