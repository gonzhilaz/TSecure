/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.presenter.impl;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabInfo;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.model.AppModel;
import com.kavsdkexample.model.AppModelStatusObserver;
import com.kavsdkexample.model.app.sdk.license.SdkLicenseInfo;
import com.kavsdkexample.presenter.MainPresenter;
import com.kavsdkexample.view.MainView;
import com.kavsdkexample.view.MainViewState;

import java.util.List;

import javax.inject.Inject;


/**
 * Presenter for main view
 * 1. Shows EULA if required
 * 2. Shows app generic permissions request
 * 3. Shows draw overlay permission request
 * 4a. Shows SDK features tabs
 * 4b. Shows License error
 */
@UiThread
public class MainPresenterImpl extends    BasePresenterImpl<MainView, MainViewState, AppModel>
                               implements MainPresenter {
    @Inject
    MainPresenterImpl(@NonNull AppModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull MainView view, @Nullable MainViewState viewState, boolean viewCreated) {
        super.subscribe(view, viewState, viewCreated);
        if (viewCreated) {
            if (mModel.isLoading()) {
                view.showProgressDialog();
            } else {
                view.hideProgressDialog();
            }
            if (viewState == null) {
                mModel.checkAcceptEula();
            }
        }
    }

    //This method is called from BasePresenterImpl.subscribe
    @Override
    protected void performAdditionalSubscriptionActions() {
        registerModelObserver(new ModelStatusObserver(mView, mModel));
    }

    @Override
    public void processGenericPermissionsResults(@NonNull List<String> grantedPermissions) {
        processPermissionsResult();
    }

    @Override
    public void checkPermissions(){
        if (mView != null) {
            mView.clearTabs();
        }
        processPermissionsResult();
        if (mModel.areAllPermissionsGranted()) {
            mView.showFeatureTabs(mModel.getTabs());
        }
    }

    @Override
    public void onSdkInited() {
        if (mView != null) {
            mView.hideProgressDialog();
            mView.showFeatureTabs(mModel.getTabs());
        }
    }

    @Override
    public void onSdkInitFailed() {
        if (mView != null) {
            mView.hideProgressDialog();
        }
    }

    @Override
    public void processOverlayPermissionResult() {
        if (mView != null) {
            mView.showInsufficientPermissionTab();
            mView.clearTabs();
        }
        processPermissionsResult();
    }

    @Override
    public void processStorageAccessPermissionResult() {
        processPermissionsResult();
    }

    @Override
    public void processAllFilesPermissionResult() {
        processPermissionsResult();
    }


    @Override
    public void processBackgroundLocationPermissionResult() {
        processPermissionsResult();
    }

    @Override
    public void processCallScreeningRoleResult() {
        processAllFilesPermissionResult();
    }

    @Override
    public void processDefaultDialerStatusResult() {
        processAllFilesPermissionResult();
    }

    @Override
    public void processIgnoreBatterySavingResult() {
        processPermissionsResult();
    }

    @Override
    public void processNotificationAccessPermissionResult() {
        processAllFilesPermissionResult();
    }

    private void processPermissionsResult() {
        if (mModel.areAllPermissionsChecked() && !mModel.areAllPermissionsGranted()) {
            if (mView != null) {
                mView.showInsufficientPermissionTab();
                return;
            } else {
                mModel.setAllPermissionsUnchecked();
            }
        }
        mModel.checkPermissions();
    }

    private void processOwnerViewEvent(SdkFeature.OwnerViewEvent event, MainView view) {
        for (SdkFeature feature: mModel.getAllFeatures()) {
            feature.onOwnerViewEvent(event, view);
        }
    }

    @Override
    public void viewCreated(@NonNull MainView view) {
        processOwnerViewEvent(SdkFeature.OwnerViewEvent.ViewCreate, view);
    }

    @Override
    public void viewDestroyed(@NonNull MainView view) {
        processOwnerViewEvent(SdkFeature.OwnerViewEvent.ViewDestroy, view);
    }

    @Override
    public void onDefaultDialerStatusRequested() {
        mModel.onDefaultDialerStatusRequested();
    }

    @Override
    public boolean isDefaultDialerStatusRequested() {
        return mModel.isDefaultDialerStatusRequested();
    }

    @Override
    public void onNotificationAccessPermissionRequested() {
        mModel.onNotificationAccessPermissionRequested();
    }

    @Override
    public boolean isNotificationAccessPermissionRequested() {
        return mModel.isNotificationAccessPermissionRequested();
    }

    @UiThread
    static class ModelStatusObserver implements AppModelStatusObserver {
        @NonNull final MainView     mView;
        @NonNull final AppModel     mModel;

        ModelStatusObserver(@NonNull MainView view, @NonNull AppModel model) {
            mView  = view;
            mModel = model;
        }

        @Override
        public void onEulaAcceptRequired(@NonNull TabInfo tabInfo) {
            mView.showTab(tabInfo);
        }

        // step 1 -> exit
        @Override
        public void onRejectEula() {
            mView.finish();
        }

        // step 1 -> step2
        @Override
        public void onAcceptEula() {
            mView.clearTabs();
            mModel.checkPermissions();
        }

        // step 1 -> step2
        @Override
        public void onSkipEula() {
            mModel.checkPermissions();
        }

        @Override
        public void onRequestGenericPermissions(@NonNull String[] requiredPermissions) {
            mView.clearTabs();
            mView.requestGenericPermissions(requiredPermissions);
        }

        @Override
        public void onRequestOverlayPermission() {
            mView.clearTabs();
            mView.requestOverlayPermission();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void onRequestStorageAccessPermissions(@NonNull List<String> sdCardPaths) {
            mView.clearTabs();
            mView.requestStorageAccessPermissions(sdCardPaths);
        }

        @Override
        @TargetApi(30)
        public void onRequestAllFilesPermission() {
            mView.clearTabs();
            mView.requestAllFilesAccessPermission();
        }

        @Override
        @TargetApi(30)
        public void onRequestBackgroundLocationPermission() {
            mView.clearTabs();
            mView.requestBackgroundLocationPermission();
        }

        @TargetApi(Build.VERSION_CODES.Q)
        @Override
        public void onRequestCallScreeningRole() {
            mView.clearTabs();
            mView.requestCallScreeningRole();
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onRequestDefaultDialerStatus() {
            mView.clearTabs();
            mView.requestDefaultDialerStatus();
        }

        @TargetApi(23)
        @Override
        public void onRequestIgnoreBatterySaving() {
            mView.clearTabs();
            mView.requestIgnoreBatterySaving();
        }

        @Override
        public void onRequestNotificationAccessPermission() {
            mView.clearTabs();
            mView.requestNotificationAccessPermission();
        }

        @Override
        public void onPermissionsDone() {
            mModel.maybeInitSdkAndFeatures(null, false);
        }

        @Override
        public void onAllPermissionsChecked() {
            mView.showInsufficientPermissionTab();
        }

        @Override
        public void onSdkLoading() {
            mView.showProgressDialog();
        }

        @Override
        public void onSdkLoadingCompleted(long loadingTimeMs) {
            mView.showSdkLoadingTime(loadingTimeMs);
        }

        @Override
        public void onSdkInitException(@NonNull SdkLicenseInfo info) {
            mView.showInitErrorTab(info.isInitialActivationFailed());
        }
    }

}