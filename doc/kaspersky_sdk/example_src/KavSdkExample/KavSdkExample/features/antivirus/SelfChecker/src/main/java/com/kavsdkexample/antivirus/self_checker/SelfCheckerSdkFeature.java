/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker;

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

import com.kavsdkexample.antivirus.self_checker.di.DaggerComponent;
import com.kavsdkexample.antivirus.self_checker.model.SelfCheckModel;
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
public final class SelfCheckerSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject ThreadManager                mThreadManager;
    @Inject SelfCheckModel               mModel;

    @UiThread
    public SelfCheckerSdkFeature(@NonNull Application            application,
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
        return SdkFeature.ANTIVIRUS_SELF_CHECKER;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }

        UiUtils.injectUi(activity, viewGroup, R.layout.self_checker_main);

        FeatureClickListener listener = new FeatureClickListener(activity);
        UiUtils.initCommandView(viewGroup, R.id.scan_self_button, R.string.str_self_checker_scan_self, listener);
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
