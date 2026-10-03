/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.kavsdkexample.antivirus.easy_scanner.di.DaggerComponent;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerModel;
import com.kavsdkexample.antivirus.easy_scanner.view.impl.EasyScannerMainFragment;
import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabInfoAndroid;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class EasyScannerSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject TabInfoFactoryAndroid        mTabInfoFactory;
    @Inject ThreadManager                mThreadManager;
    @Inject EasyScannerModel             mModel;


    @UiThread
    public EasyScannerSdkFeature(@NonNull Application            application,
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
        return SdkFeature.ANTIVIRUS_EASY_SCANNER;
    }

    @Override
    public TabInfoAndroid provideFeatureTab(@Nullable TabCompletionCallback callback) {
        return mTabInfoFactory.createTabInfo(mContext.getString(R.string.str_antivirus_easy_scanner_feature_tab), EasyScannerMainFragment.class.getName());
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
