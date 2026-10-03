/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.ui.UiProvider;
import com.kavsdkexample.core.ui.UiProviderImpl;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.TabInfoFactoryImpl;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;
import com.kavsdkexample.fingerprintmonitor.model.FingerprintMonitorModel;
import com.kavsdkexample.fingerprintmonitor.model.Settings;
import com.kavsdkexample.fingerprintmonitor.model.impl.FingerprintMonitorModelImpl;
import com.kavsdkexample.fingerprintmonitor.model.impl.SettingsImpl;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
abstract class FeatureModule {
    @Provides
    @NonNull
    static Context provideContext(@NonNull Application application) {
        return application.getApplicationContext();
    }

    @Provides
    @NonNull
    static ExecutorService provideExecutorService() {
        return AppThreadPool.getExecutorService();
    }

    @Provides
    @NonNull
    static UiProvider provideUiProvider(@NonNull Context context) {
        return new UiProviderImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static TabInfoFactoryAndroid provideTabInfoFactory() {
        return new TabInfoFactoryImpl();
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }

    @Provides
    @Singleton
    @NonNull
    static FingerprintMonitorModel provideFingerprintMonitorModel(@NonNull ThreadManager threadManager,
                                                                   @NonNull Settings settings,
                                                                   @NonNull UiProvider uiProvider,
                                                                   @NonNull Context context) {
        return new FingerprintMonitorModelImpl(threadManager, settings, uiProvider, context);
    }


    @Provides
    @NonNull
    static Settings provideSettings(@NonNull Context context) {
        return new SettingsImpl(context);
    }
}
