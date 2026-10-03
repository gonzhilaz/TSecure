/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.view;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.features.TabDescription;
import com.kavsdkexample.core.app.features.TabInfo;
import com.kavsdkexample.core.app.view.BaseView;

import java.util.List;

@UiThread
public interface MainView extends BaseView {
    void showTab(@NonNull TabInfo tabInfo);
    void showInsufficientPermissionTab();
    void showFeatureTabs(@NonNull List<TabDescription> tabs);
    void showInitErrorTab(boolean licenseError);
    void clearTabs();
    void requestGenericPermissions(@NonNull String[] permissions);
    void requestOverlayPermission();
    @TargetApi(Build.VERSION_CODES.M)
    void requestStorageAccessPermissions(@NonNull List<String> sdCardPaths);
    @TargetApi(30)
    void requestAllFilesAccessPermission();
    @TargetApi(30)
    void requestBackgroundLocationPermission();
    @TargetApi(Build.VERSION_CODES.Q)
    void requestCallScreeningRole();
    @TargetApi(Build.VERSION_CODES.M)
    void requestDefaultDialerStatus();
    void requestNotificationAccessPermission();
    void finish();
    void showSdkLoadingTime(long loadingTimeMs);
    @TargetApi(Build.VERSION_CODES.R)
    void requestObbDirPermission();
    @TargetApi(Build.VERSION_CODES.R)
    void requestDataDirPermission();
    void requestDocumentPermission(String data, int code);
    @TargetApi(23)
    void requestIgnoreBatterySaving();
}