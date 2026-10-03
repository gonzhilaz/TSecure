/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.di;

import com.kavsdkexample.model.app.impl.BaseApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.support.AndroidSupportInjectionModule;

@Singleton
@Component(modules = { AndroidSupportInjectionModule.class,
                       AndroidInjectionModule.class,
                       ActivityBuilder.class,
                       ServiceBuilder.class,
                       BroadcastBuilder.class,
                       AppModule.class })
public interface AppComponent {
    void inject(BaseApplication app);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(BaseApplication application);
        AppComponent build();
    }
}

