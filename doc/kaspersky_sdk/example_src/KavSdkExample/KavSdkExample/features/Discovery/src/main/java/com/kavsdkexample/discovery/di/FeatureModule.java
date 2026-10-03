/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.discovery.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;
import com.kavsdkexample.discovery.model.DiscoveryModel;
import com.kavsdkexample.discovery.model.impl.DiscoveryModelImpl;
import com.kavsdkexample.discovery.sdk.SdkDiscovery;
import com.kavsdkexample.discovery.sdk.SdkDiscoveryListener;
import com.kavsdkexample.discovery.sdk.impl.SdkDiscoveryImpl;
import com.kavsdkexample.discovery.sdk.impl.SdkDiscoveryListenerImpl;

@Module
class FeatureModule {
    @Provides
    @NonNull
    static Context provideContext(@NonNull Application application) {
        return application.getApplicationContext();
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }

    @Provides
    @NonNull
    static SdkDiscoveryListener provideSdkDiscoveryListener(@NonNull Context context) {
        return new SdkDiscoveryListenerImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static SdkDiscovery provideSdkDiscovery() {
        return new SdkDiscoveryImpl();
    }

    @Provides
    @Singleton
    @NonNull
    static DiscoveryModel provideDiscoveryModel(@NonNull ThreadManager threadManager, @NonNull SdkDiscovery sdkDiscovery, @NonNull SdkDiscoveryListener listener) {
        return new DiscoveryModelImpl(threadManager, sdkDiscovery, listener);
    }
}
