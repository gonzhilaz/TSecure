/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.permissions.impl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Build;

import java.util.List;

public final class PermissionRepositoryHelper {
    @SuppressLint("NewApi")
    static void addPermissions(List<String> permissionsList) {
        permissionsList.add(Manifest.permission.READ_PHONE_STATE);
        permissionsList.add(Manifest.permission.SEND_SMS);
        permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsList.add(Manifest.permission.READ_CONTACTS);
        permissionsList.add(Manifest.permission.READ_CALL_LOG);
        permissionsList.add(Manifest.permission.READ_SMS);
        permissionsList.add(Manifest.permission.CALL_PHONE);
        permissionsList.add(Manifest.permission.RECEIVE_SMS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionsList.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissionsList.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            permissionsList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // We need request READ_EXTERNAL_STORAGE, the WRITE_EXTERNAL_STORAGE is not enough, especially on Android 9
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private PermissionRepositoryHelper() {
    }
}
