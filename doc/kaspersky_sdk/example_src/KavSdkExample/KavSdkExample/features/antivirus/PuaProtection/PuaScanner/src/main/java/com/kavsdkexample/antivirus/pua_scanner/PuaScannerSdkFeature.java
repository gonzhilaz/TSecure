/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner;

import android.app.Application;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.kavsdkexample.antivirus.pua_scanner.di.DaggerComponent;
import com.kavsdkexample.antivirus.pua_scanner.model.PuaScannerModel;
import com.kavsdkexample.antivirus.pua_scanner.view.FeatureClickListener;
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
public class PuaScannerSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject ThreadManager              mThreadManager;
    @Inject PuaScannerModel            mModel;

    @UiThread
    public PuaScannerSdkFeature(@NonNull Application application,
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

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.ANTIVIRUS_PUA_SCANNER;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull Fragment fragment, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }

        UiUtils.injectUi(fragment.requireActivity(), viewGroup, R.layout.pua_scanner_main);

        FeatureClickListener listener = new FeatureClickListener(fragment);
        UiUtils.initCommandView(viewGroup,
                                R.id.pua_scanner_button,
                                R.string.str_pua_scanner_button,
                                listener);
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
