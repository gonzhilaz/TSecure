/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.permissions.impl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telecom.TelecomManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.kavsdkexample.model.app.permissions.PermissionRepository;

import java.util.ArrayList;
import java.util.List;

public abstract class PermissionRepositoryBase implements PermissionRepository {

    private static final String NOTIFICATION_ACCESS_PERMISSION = "NotificationAccessPermission";

    private final Context mContext;
    private boolean       mCanDrawOverlays;
    private AppOpsManager.OnOpChangedListener mOpChangedListener;

    PermissionRepositoryBase(@NonNull Context context) {
        mContext = context;
        // Workaround for an issue with Settings.canDrawOverlays: https://issuetracker.google.com/issues/66072795
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            mCanDrawOverlays = Settings.canDrawOverlays(context);
            mOpChangedListener = (op, packageName) -> {
                String myPackageName = mContext.getPackageName();
                if (myPackageName.equals(packageName) &&
                        AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW.equals(op)) {
                    mCanDrawOverlays = !mCanDrawOverlays;
                }
            };
            opsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                    null, mOpChangedListener);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mOpChangedListener != null) {
            AppOpsManager opsManager = (AppOpsManager)mContext.getSystemService(Context.APP_OPS_SERVICE);
            opsManager.stopWatchingMode(mOpChangedListener);
            mOpChangedListener = null;
        }
        super.finalize();
    }

    @NonNull
    @Override
    @SuppressLint("NewApi")
    public List<String> getRequiredAdditionalPermissions() {
        List<String> required = new ArrayList<>();
        if (!checkOverlayPermission()) {
            required.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!checkAllFilesPermission()) {
                required.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            }
            if (!checkBackgroundLocationPermission()) {
                required.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }
        if (!checkCallScreeningServiceRole()) {
            required.add(RoleManager.ROLE_CALL_SCREENING);
        }
        if (!checkDefaultDialerStatus()) {
            required.add(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        }
        if (!checkNotificationAccessPermission()) {
            required.add(NOTIFICATION_ACCESS_PERMISSION);
        }
        return required;
    }

    @Override
    @NonNull
    public List<String> getRequiredGenericPermissions() {
        List<String> requiredPermissions = new ArrayList<>(getSdkNecessaryPermissions().size());
        for (String permission : getSdkNecessaryPermissions()) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(permission);
            }
        }
        return requiredPermissions;
    }

    @Override
    public boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(mContext)) {
                return true;
            } else {
                return mCanDrawOverlays;
            }
        } else {
            return true;
        }
    }

    protected Context getContext() {
        return mContext;
    }

    @NonNull
    protected abstract List<String> getSdkNecessaryPermissions();
}
