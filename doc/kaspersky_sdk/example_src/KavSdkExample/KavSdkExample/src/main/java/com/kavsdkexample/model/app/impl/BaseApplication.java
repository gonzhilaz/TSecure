/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.impl;

import android.annotation.SuppressLint;
import androidx.annotation.Keep;
import androidx.annotation.UiThread;
import androidx.multidex.MultiDexApplication;

import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.model.interactors.ManageableServiceInteractor;
import com.kavsdkexample.di.DaggerAppComponent;
import com.kavsdkexample.model.SdkFeatureManager;
import com.kavsdkexample.model.app.SdkInitializer;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.HasAndroidInjector;

@SuppressLint("Registered")
@Keep
public class BaseApplication extends MultiDexApplication
                             implements HasAndroidInjector {

    @Inject ExtendableInjector<Object>            mExtendableInjector;

    @Inject SdkInitializer                        mSdkInitializer;
    @Inject SdkFeatureManager                     mSdkFeatureManager;
    @Inject ManageableServiceInteractor           mServiceInteractor;

    @Override
    @UiThread
    public void onCreate() {
        super.onCreate();

        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this);
        mSdkFeatureManager.combineFeaturesInjectors(mExtendableInjector);

        mServiceInteractor.setServiceManagementFeature(mSdkFeatureManager.findSdkFeature(SdkFeature.SELF_DEFENSE));
        mSdkInitializer.maybeInitSdkAndFeatures(mSdkFeatureManager, false);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }

    public SdkInitializer getSdkInitializer() {
        return mSdkInitializer;
    }
}
