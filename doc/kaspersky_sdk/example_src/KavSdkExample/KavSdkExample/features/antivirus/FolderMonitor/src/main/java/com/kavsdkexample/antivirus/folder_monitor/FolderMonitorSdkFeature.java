/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor;

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

import com.kavsdkexample.antivirus.folder_monitor.di.DaggerComponent;
import com.kavsdkexample.antivirus.folder_monitor.model.FolderMonitorModel;
import com.kavsdkexample.antivirus.folder_monitor.view.impl.FeatureClickListener;
import com.kavsdkexample.core.app.ExtendableInjector;
import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.SdkFeatureAndroidImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.view.utils.UiUtils;

import javax.inject.Inject;

import dagger.android.AndroidInjector;

@Keep
public final class FolderMonitorSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject TabInfoFactoryAndroid        mTabInfoFactory;
    @Inject ThreadManager                mThreadManager;
    @Inject FolderMonitorModel           mModel;

    @Override
    protected boolean dependsOnSdkInit() {
        return false;   // TODO: remove when Model will be available
    }

    @UiThread
    public FolderMonitorSdkFeature(@NonNull Application            application,
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
        return SdkFeature.ANTIVIRUS_FOLDER_MONITOR;
    }

    @Override
    public boolean checkHandleIntent(@Nullable Intent intent, @Nullable FragmentActivity parent) {
        return false;
    }

    @Override
    @UiThread
    public void initUiView(@NonNull Fragment fragment, @NonNull ViewGroup viewGroup) {
        if (BuildConfig.DEBUG) {
            mThreadManager.checkUiThread();
        }

        UiUtils.injectUi(fragment.requireActivity(), viewGroup, R.layout.folder_monitor_main);

        FeatureClickListener listener = new FeatureClickListener(fragment);
        UiUtils.initCommandView(viewGroup, R.id.folder_monitor_button, R.string.str_av_folder_monitor_button, listener);
    }


    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
