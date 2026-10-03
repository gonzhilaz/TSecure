/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.single_thread_scanner.di;

import android.app.Application;

import com.kavsdkexample.antivirus.single_thread_scanner.SingleThreadScannerSdkFeature;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@dagger.Component(modules = {
        AndroidSupportInjectionModule.class,
        AndroidInjectionModule.class,
        ActivityBuilder.class,
        FragmentsBuilder.class,
        FeatureModule.class })
public interface Component {
    void inject(SingleThreadScannerSdkFeature feature);

    @dagger.Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        Component build();
    }

}

