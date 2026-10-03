/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.view.utils.UiUtils;
import com.kavsdkexample.fingerprintmonitor.di.DaggerComponent;
import com.kavsdkexample.fingerprintmonitor.model.FingerprintMonitorModel;
import com.kavsdkexample.fingerprintmonitor.view.impl.FingerprintMonitorActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public class FingerprintMonitorSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject ThreadManager              mThreadManager;
    @Inject FingerprintMonitorModel    mModel;

    public FingerprintMonitorSdkFeature(@NonNull Application application,
                                           @NonNull ServiceInteractor serviceInteractor,
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

    @NonNull
    @Override
    public String getName() {
        return SdkFeature.FINGERPRINT_MONITOR;
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

        UiUtils.injectUi(activity, viewGroup, R.layout.fingerprint_monitor_main);
        UiUtils.initCommandView(viewGroup, R.id.btn_fingerprint_monitor, R.string.str_fingerprint_monitor_button, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, FingerprintMonitorActivity.class));
            }
        });
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
