/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.discovery;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.FragmentActivity;
import android.view.ViewGroup;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.view.utils.UiUtils;
import com.kavsdkexample.discovery.di.DaggerComponent;
import com.kavsdkexample.discovery.model.DiscoveryModel;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class DiscoverySdkFeature extends SdkFeatureAndroidImpl {
    @Inject  ExtendableInjector<Object>   mExtendableInjector;
    @Inject  ThreadManager                mThreadManager;
    @Inject  DiscoveryModel               mModel;


    public DiscoverySdkFeature(@NonNull Application            application,
                              @NonNull ServiceInteractor      serviceInteractor,
                              @NonNull NotificationInteractor notificationInteractor) {
        super(application, serviceInteractor, notificationInteractor);
        DaggerComponent.builder()
                .application(application)
                .build()
                .inject(this);
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
        mModel.setSdkStatusProvider(this);
    }

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.DISCOVERY;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
    }

    @Override
    public boolean checkHandleIntent(@Nullable Intent intent, @Nullable FragmentActivity parent) {
        return false;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
