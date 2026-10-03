/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabInfoAndroid;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.eula.di.DaggerComponent;
import com.kavsdkexample.eula.model.EulaManageableModel;
import com.kavsdkexample.eula.model.settings.Settings;
import com.kavsdkexample.eula.view.impl.EulaFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class EulaSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject Settings                     mSettings;
    @Inject EulaManageableModel          mModel;
    @Inject TabInfoFactoryAndroid        mTabInfoFactory;
    @Inject ThreadManager                mThreadManager;

    @UiThread
    public EulaSdkFeature(@NonNull Application            application,
                          @NonNull ServiceInteractor      serviceInteractor,
                          @NonNull NotificationInteractor notificationInteractor) {
        super(application, serviceInteractor, notificationInteractor);

        DaggerComponent
            .builder()
            .application(application)
            .build()
            .inject(this);

        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }
    }

    @Override
    protected boolean dependsOnSdkInit() {
        return false;
    }

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.EULA;
    }

    @Override
    @Nullable
    public TabInfoAndroid provideFeatureTab(@Nullable TabCompletionCallback callback) {
        if (mSettings.isEulaAccepted()) {
            // Already accepted, don't want to show any UI
            return null;
        }

        mModel.setTabCompletionCallback(callback);

        return mTabInfoFactory.createTabInfo(mContext.getString(R.string.str_strings_eula_fragment_tab), EulaFragment.class.getName());
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
