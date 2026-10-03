/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model;

import android.annotation.TargetApi;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import android.os.Build;

import com.kavsdkexample.core.app.features.TabInfo;
import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;

import java.util.List;

@UiThread
public interface AppModelStatusObserver {
    void onEulaAcceptRequired(@NonNull TabInfo tabInfo);
    void onRejectEula();
    void onAcceptEula();
    void onSkipEula();
    void onRequestGenericPermissions(@NonNull String[] requiredPermissions);
    void onRequestOverlayPermission();
    @TargetApi(29)
    void onRequestStorageAccessPermissions(@NonNull List<String> sdCardPaths);
    @TargetApi(30)
    void onRequestAllFilesPermission();
    @TargetApi(30)
    void onRequestBackgroundLocationPermission();
    @TargetApi(Build.VERSION_CODES.Q)
    void onRequestCallScreeningRole();
    @TargetApi(Build.VERSION_CODES.M)
    void onRequestDefaultDialerStatus();
    void onRequestNotificationAccessPermission();
    void onPermissionsDone();
    void onAllPermissionsChecked();
    void onSdkLoading();
    void onSdkLoadingCompleted(long loadingTimeMs);
    void onSdkInitException(@NonNull SdkLicenseInfo info);
    @TargetApi(23)
    void onRequestIgnoreBatterySaving();
}