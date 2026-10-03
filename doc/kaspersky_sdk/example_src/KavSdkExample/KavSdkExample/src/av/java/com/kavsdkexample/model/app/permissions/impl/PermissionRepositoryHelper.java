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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionsList.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
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