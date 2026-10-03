/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.interactors.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.SdkFeatureAndroid;
import com.kavsdkexample.core.app.model.interactors.ForegroundCaller;
import com.kavsdkexample.core.app.model.interactors.ManageableServiceInteractor;
import com.kavsdkexample.model.service.SampleService;

public final class ServiceInteractorImpl implements ManageableServiceInteractor {
    @NonNull
    private final Context    mContext;
    @Nullable
    private       SdkFeature mServiceManagementFeature;

    public ServiceInteractorImpl(@NonNull Context context) {
        mContext            = context;
    }

    @Override
    public void setServiceManagementFeature(@Nullable SdkFeature feature) {
        mServiceManagementFeature = feature;
    }

    @Override
    public boolean startService(@NonNull ForegroundCaller caller) {
        if (mServiceManagementFeature == null) {
            return false;
        } else {
            if (mServiceManagementFeature instanceof SdkFeatureAndroid &&
                (caller != ForegroundCaller.SelfDefense || ((SdkFeatureAndroid) mServiceManagementFeature).checkHandleIntent(null, null))) {
                SampleService.startService(mContext, caller);
                return true;
            } else {
                SampleService.startService(mContext, ForegroundCaller.ForegroundNotRequired);
                return false;
            }
        }
    }

    @Override
    public void stopService(@NonNull ForegroundCaller caller) {
        if (mServiceManagementFeature != null && mServiceManagementFeature instanceof SdkFeatureAndroid &&
            (caller != ForegroundCaller.SelfDefense || ((SdkFeatureAndroid)mServiceManagementFeature).checkHandleIntent(null, null))) {

            SampleService.stopForegroundService(mContext, caller);
        }
    }
}
