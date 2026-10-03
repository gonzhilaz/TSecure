/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.Fragment;

import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.update.di.DaggerComponent;
import com.kavsdkexample.update.view.impl.UpdateFragment;
import com.kavsdkexample.update.model.UpdateModel;
import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.TabInfoAndroid;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class UpdateSdkFeature extends SdkFeatureAndroidImpl {

    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject TabInfoFactoryAndroid        mTabInfoFactory;
    @Inject ThreadManager                mThreadManager;
    @Inject UpdateModel                  mModel;

    @UiThread
    public UpdateSdkFeature(@NonNull Application            application,
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
        return SdkFeature.UPDATE;
    }

    @Override
    public TabInfoAndroid provideFeatureTab(@Nullable TabCompletionCallback callback) {
        return mTabInfoFactory.createTabInfo(mContext.getString(R.string.str_updates_feature_tab), UpdateFragment.class.getName());
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
