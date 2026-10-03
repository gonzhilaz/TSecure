/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.permissions.impl;

import android.Manifest;
import android.app.ActivityManager;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.kaspersky.whocalls.messengers.WhoCallsNotificationService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PermissionRepositoryImpl extends PermissionRepositoryBase {

    private static final String KEY_FRAGMENT_ARGS = ":settings:fragment_args_key";
    private static final String KEY_FRAGMENT_SHOW_ARGS = ":settings:show_fragment_args";

    private static final List<String> SDK_NECESSARY_PERMISSIONS = new ArrayList<>();

    static {
        SDK_NECESSARY_PERMISSIONS.add(Manifest.permission.READ_PHONE_STATE);
        SDK_NECESSARY_PERMISSIONS.add(Manifest.permission.READ_CALL_LOG);
        SDK_NECESSARY_PERMISSIONS.add(Manifest.permission.READ_CONTACTS);
    }

    public PermissionRepositoryImpl(@NonNull final Context context) {
        super(context);
    }

    @Override
    public boolean checkCallScreeningServiceRole() {
        if (android.os.Build.VERSION.SDK_INT >= 29) { // lint does not understand VERSION_CODE constant for some reason
            final RoleManager roleManager = (RoleManager) getContext()
                    .getSystemService(Context.ROLE_SERVICE);
            return !isCallScreeningRoleAvailable()
                    || roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
        } else {
            return true;
        }
    }

    private boolean isCallScreeningRoleAvailable() {
        if (Build.VERSION.SDK_INT >= 29) { // lint does not understand VERSION_CODE constant for some reason
            return ((RoleManager) getContext().getSystemService(Context.ROLE_SERVICE))
                    .isRoleAvailable(RoleManager.ROLE_CALL_SCREENING);
        } else {
            return false;
        }
    }

    @Override
    public boolean checkDefaultDialerStatus() {
        if (isCallScreeningRoleAvailable()) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ((TelecomManager) getContext()
                    .getSystemService(Context.TELECOM_SERVICE))
                    .getDefaultDialerPackage()
                    .equals(getContext().getPackageName());
        } else {
            return true;
        }
    }

    @Override
    public boolean checkNotificationAccessPermission() {
        if (isNotificationListenerServiceAvailable()) {
            return NotificationManagerCompat
                    .getEnabledListenerPackages(getContext())
                    .contains(getContext().getPackageName());
        } else {
            return true;
        }
    }

    private boolean isNotificationListenerServiceAvailable() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.Q || !getActivityManager().isLowRamDevice();
    }

    private ActivityManager getActivityManager() {
        return (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
    }

    @SuppressLint("InlinedApi") // TODO: check for shooting ourselves in the leg
    @Override
    public void requestNotificationAccessPermission() {
        ComponentName componentName = new ComponentName(getContext(), WhoCallsNotificationService.class);
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, componentName.flattenToString());
        } else {
            intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(KEY_FRAGMENT_ARGS, componentName.flattenToString());
            Bundle showArgs = new Bundle();
            showArgs.putString(KEY_FRAGMENT_ARGS, componentName.flattenToString());
            intent.putExtra(KEY_FRAGMENT_SHOW_ARGS, showArgs);
        }
        getContext().startActivity(intent);
    }

    @NonNull
    @Override
    protected List<String> getSdkNecessaryPermissions() {
        return Collections.unmodifiableList(SDK_NECESSARY_PERMISSIONS);
    }

    @Override
    public boolean checkAllFilesPermission() {
        return true;
    }

    @Override
    public boolean checkBackgroundLocationPermission() {
        return true;
    }

    @Override
    public boolean checkIgnoreBatterySaving() {
        return true;
    }

    @NonNull
    @Override
    public List<String> getSdCardPathsWithMissingPermissions() {
        return Collections.emptyList();
    }
}
