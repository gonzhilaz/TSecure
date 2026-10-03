/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor;

import android.app.Application;
import android.content.Intent;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.ViewGroup;

import com.kavsdkexample.antivirus.pua_monitor.di.DaggerComponent;
import com.kavsdkexample.antivirus.pua_monitor.model.PuaMonitorModel;
import com.kavsdkexample.antivirus.pua_monitor.view.impl.FeatureClickListener;
import com.kavsdkexample.antivirus.base.model.ThreadType;
import com.kavsdkexample.antivirus.base.monitor.sdk.impl.MonitorsWorkWatcherImpl;
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
public final class PuaMonitorSdkFeature extends SdkFeatureAndroidImpl {
    @Inject ExtendableInjector<Object>   mExtendableInjector;
    @Inject ThreadManager                mThreadManager;
    @Inject PuaMonitorModel              mModel;

    @UiThread
    public PuaMonitorSdkFeature(@NonNull Application            application,
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
        mModel.runAntivirusAction(() -> MonitorsWorkWatcherImpl.getInstance(mThreadManager, serviceInteractor), ThreadType.Ui);
    }

    @Override
    @NonNull
    public String getName() {
        return SdkFeature.ANTIVIRUS_PUA_MONITOR;
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

        UiUtils.injectUi(fragment.requireActivity(), viewGroup, R.layout.pua_monitor_main);

        FeatureClickListener listener = new FeatureClickListener(fragment);
        UiUtils.initCommandView(viewGroup, R.id.pua_monitor_button, R.string.str_pua_monitor_button, listener);
    }


    @Override
    public AndroidInjector<Object> androidInjector() {
        return mExtendableInjector;
    }
}
