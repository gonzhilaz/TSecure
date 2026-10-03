/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.di;

import android.app.Application;

import com.kavsdkexample.antivirus.rtp_monitor.RtpMonitorSdkFeature;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@dagger.Component(modules = {
        AndroidSupportInjectionModule.class,
        AndroidInjectionModule.class,
        FragmentsBuilder.class,
        FeatureModule.class })
public interface Component {
    void inject(RtpMonitorSdkFeature feature);

    @dagger.Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        Component build();
    }

}

