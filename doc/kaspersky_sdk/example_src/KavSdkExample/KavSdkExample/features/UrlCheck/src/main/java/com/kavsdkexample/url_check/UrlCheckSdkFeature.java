/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.url_check;

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
import com.kavsdkexample.url_check.di.DaggerComponent;
import com.kavsdkexample.url_check.nonmvp.FeatureClickListener;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class UrlCheckSdkFeature extends SdkFeatureAndroidImpl {

    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject ThreadManager              mThreadManager;


    public UrlCheckSdkFeature(@NonNull Application            application,
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
    }

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.URL_CHECK;
    }

    @Override
    protected boolean dependsOnSdkInit() {
        return false;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }

        UiUtils.injectUi(activity, viewGroup, R.layout.url_check_main);

        FeatureClickListener listener = new FeatureClickListener(activity);
        UiUtils.initCommandView(viewGroup, R.id.check_url_reputation_button, R.string.str_url_check_check_url_button, listener);
        UiUtils.initCommandView(viewGroup, R.id.check_url_reputation_ext_button, R.string.str_url_check_check_url_ext_button, listener);
        UiUtils.initCommandView(viewGroup, R.id.check_financial_category_button, R.string.str_url_check_check_bank_url_button, listener);
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
