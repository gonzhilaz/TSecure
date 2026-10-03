/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.permissions.impl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.kavsdkexample.core.app.utils.PermissionUtils;
import com.kavsdkexample.BuildConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionRepositoryAvFullBase extends PermissionRepositoryBase {

    private static final List<String> SDK_NECESSARY_PERMISSIONS = new ArrayList<>();
    private boolean mDoNotRequestIgnoreBatterySavingPermission = true; //NOPMD

    static {
        PermissionRepositoryHelper.addPermissions(SDK_NECESSARY_PERMISSIONS);
    }

    public PermissionRepositoryAvFullBase(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected List<String> getSdkNecessaryPermissions() {
        return Collections.unmodifiableList(SDK_NECESSARY_PERMISSIONS);
    }

    @Override
    @NonNull public List<String> getSdCardPathsWithMissingPermissions() {
        List<String> result = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return result;
        }
        List<String> sdCardPaths = com.kavsdk.utils.Utils.getStoragePaths(getContext(), Integer.MAX_VALUE);
        for (String sdCardPath : sdCardPaths) {
            if (PermissionUtils.ifSdCardNeedsPermissionToAccess(getContext(), sdCardPath) && !PermissionUtils.hasPermissionToAccessSdCard(getContext(), sdCardPath)) {
                result.add(sdCardPath);
            }
        }
        return result;
    }

    @Override
    @SuppressLint("NewApi")
    public boolean checkAllFilesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return true;
        }
    }

    @Override
    @SuppressLint("NewApi")
    public boolean checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return  ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @Override
    //NOPMD
    public boolean checkIgnoreBatterySaving() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final PowerManager powerManager = (PowerManager)getContext().getSystemService(Context.POWER_SERVICE);
            return mDoNotRequestIgnoreBatterySavingPermission /* do not request permission, but keep code around for future use */ || powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID);
        } else {
            return true;
        }
    }

    @Override
    public boolean checkCallScreeningServiceRole() {
        return true;
    }

    @Override
    public boolean checkDefaultDialerStatus() {
        return true;
    }

    @Override
    public boolean checkNotificationAccessPermission() {
        return true;
    }

    @Override
    public void requestNotificationAccessPermission() {
        // do nothing
    }
}
