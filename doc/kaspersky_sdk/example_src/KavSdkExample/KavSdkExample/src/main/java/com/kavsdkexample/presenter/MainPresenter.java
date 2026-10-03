/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.view.MainView;
import com.kavsdkexample.view.MainViewState;

import java.util.List;

public interface MainPresenter extends BasePresenter<MainView, MainViewState> {
    void processGenericPermissionsResults(@NonNull List<String> grantedPermissions);
    void checkPermissions();
    void processOverlayPermissionResult();
    void processStorageAccessPermissionResult();
    void processAllFilesPermissionResult();
    void processBackgroundLocationPermissionResult();
    void processCallScreeningRoleResult();
    void processDefaultDialerStatusResult();
    void processIgnoreBatterySavingResult();
    void processNotificationAccessPermissionResult();
    void viewCreated(@NonNull MainView view);
    void viewDestroyed(@NonNull MainView view);
    void onDefaultDialerStatusRequested();
    boolean isDefaultDialerStatusRequested();
    void onNotificationAccessPermissionRequested();
    boolean isNotificationAccessPermissionRequested();
}