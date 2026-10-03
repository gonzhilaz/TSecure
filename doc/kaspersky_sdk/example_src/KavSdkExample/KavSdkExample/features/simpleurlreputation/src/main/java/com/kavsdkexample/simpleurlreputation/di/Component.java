/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.simpleurlreputation.di;

import android.app.Application;

import com.kavsdkexample.simpleurlreputation.SimpleUrlReputationSdkFeature;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@dagger.Component(modules = {
        AndroidSupportInjectionModule.class,
        AndroidInjectionModule.class,
        FragmentsBuilder.class,
        FeatureModel.class
})
public interface Component {
    void inject(SimpleUrlReputationSdkFeature feature);

    @dagger.Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        Component build();
    }
}
