/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

public final class AppControlUtils {
    public static void launchHomeScreen(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> homeActivitiesResInfo = pm.queryIntentActivities(intent, 0);
        if (homeActivitiesResInfo.size() > 0 || getHomeActivityInfo(pm) != null) {
            context.startActivity(intent);
        }
    }

    public static void launchHomeScreen(Activity activity, boolean finishActivity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> homeActivitiesResInfo = pm.queryIntentActivities(intent, 0);
        if (homeActivitiesResInfo.size() > 0 || getHomeActivityInfo(pm) != null) {
            activity.startActivity(intent);
            if (finishActivity) {
                activity.finish();
            }
        }
    }

    public static ResolveInfo getHomeActivityInfo(final PackageManager pm) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }
}