/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ViewGroup;

import com.kavsdkexample.appcontrol.di.DaggerComponent;
import com.kavsdkexample.appcontrol.view.impl.FeatureClickListener;
import com.kavsdkexample.appcontrol.model.AppControlModel;
import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class AppControlSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject ThreadManager                mThreadManager;
    @Inject AppControlModel              mModel;

    @Override
    protected boolean dependsOnSdkInit() {
        return false;   // TODO: remove when Model will be available
    }

    @UiThread
    public AppControlSdkFeature(@NonNull Application            application,
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
        return SdkFeature.APP_CONTROL;
    }

    @Override
    public boolean checkHandleIntent(@Nullable Intent intent, @Nullable FragmentActivity parent) {
        return false;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }

        UiUtils.injectUi(activity, viewGroup, R.layout.app_control_main);

        FeatureClickListener listener = new FeatureClickListener(activity);
        UiUtils.initCommandView(viewGroup, R.id.app_control_button, R.string.str_app_control_button, listener);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
