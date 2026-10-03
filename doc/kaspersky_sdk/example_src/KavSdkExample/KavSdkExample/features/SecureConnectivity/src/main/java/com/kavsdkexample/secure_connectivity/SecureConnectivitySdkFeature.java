/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity;

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
import com.kavsdkexample.secure_connectivity.di.DaggerComponent;
import com.kavsdkexample.secure_connectivity.model.SecureConnectionModel;
import com.kavsdkexample.secure_connectivity.nonmvp.FeatureClickListener;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class SecureConnectivitySdkFeature extends SdkFeatureAndroidImpl {

    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject SecureConnectionModel      mModel;
    @Inject ThreadManager              mThreadManager;


    public SecureConnectivitySdkFeature(@NonNull Application            application,
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
        return SdkFeature.SECURE_CONNECTION;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull FragmentActivity activity, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
        UiUtils.injectUi(activity, viewGroup, R.layout.secure_connectivity_main);

        FeatureClickListener listener = new FeatureClickListener(activity);
        UiUtils.initCommandView(viewGroup, R.id.dns_checker_button, R.string.str_secure_connectivity_open_dns_checker, listener);
        UiUtils.initCommandView(viewGroup, R.id.test_secure_connection_button, R.string.str_secureconnection_test_button, listener);
        UiUtils.initCommandView(viewGroup, R.id.cert_validator_button, R.string.str_secure_connectivity_check_certificate_url, listener);
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
