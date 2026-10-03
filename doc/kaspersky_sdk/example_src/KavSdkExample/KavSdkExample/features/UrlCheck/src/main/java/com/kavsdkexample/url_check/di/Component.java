/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.url_check.di;

import android.app.Application;

import com.kavsdkexample.url_check.UrlCheckSdkFeature;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@dagger.Component(modules = {
        AndroidSupportInjectionModule.class,
        AndroidInjectionModule.class,
        FeatureModule.class})
public interface Component {
    void inject(UrlCheckSdkFeature feature);

    @dagger.Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        Component build();
    }
}

