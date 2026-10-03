/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense;

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
import com.kavsdkexample.self_defense.di.DaggerComponent;
import com.kavsdkexample.self_defense.model.ManageableSelfDefenseModel;
import com.kavsdkexample.self_defense.model.settings.Settings;
import com.kavsdkexample.self_defense.view.impl.SelfDefenseMainFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class SelfDefenseSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object> mExtendableInjector;
    @Inject TabInfoFactoryAndroid      mTabInfoFactory;
    @Inject Settings                   mSettings;
    @Inject ThreadManager              mThreadManager;
    @Inject ManageableSelfDefenseModel mModel;

    @UiThread
    public SelfDefenseSdkFeature(@NonNull Application            application,
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

        mModel.setSdkStatusProvider(this);
        mModel.setInteractors(serviceInteractor, notificationInteractor);
    }

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.SELF_DEFENSE;
    }

    @Override
    @NonNull
    public TabInfoAndroid provideFeatureTab(@Nullable TabCompletionCallback callback) {
        return mTabInfoFactory.createTabInfo(mContext.getString(R.string.str_self_defense_tab), SelfDefenseMainFragment.class.getName());
    }

    /**
     * As a result of procession any intent this feature returns is a foreground service is enabled for this app or not
     * @param intent not used
     * @param parent not used
     * @return true if foreground service is enabled or false otherwise
     */
    @Override
    public boolean checkHandleIntent(@Nullable Intent intent, @Nullable FragmentActivity parent) {
        return mSettings.isForegroundServiceEnabled();
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
