/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.TabInfoFactoryImpl;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;
import com.kavsdkexample.self_defense.model.ManageableSelfDefenseModel;
import com.kavsdkexample.self_defense.model.SelfDefenseModel;
import com.kavsdkexample.self_defense.model.impl.SelfDefenseModelImpl;
import com.kavsdkexample.self_defense.model.sdk.SdkManager;
import com.kavsdkexample.self_defense.model.sdk.impl.SdkManagerImpl;
import com.kavsdkexample.self_defense.model.settings.Settings;
import com.kavsdkexample.self_defense.model.settings.impl.SettingsImpl;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import dagger.Binds;
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
    @Singleton
    @NonNull
    static SdkManagerImpl provideSdkManager(@NonNull Context         context,
                                            @NonNull ExecutorService executorService,
                                            @NonNull ThreadManager   threadManager) {
        return new SdkManagerImpl(context, executorService, threadManager);
    }

    @Binds
    @NonNull
    abstract SdkManager bindSdkManager(SdkManagerImpl sdkManager);

    @Provides
    @Singleton
    @NonNull
    static ManageableSelfDefenseModel provideManageableModel(@NonNull SdkManager       sdkManager,
                                                             @NonNull Settings         settings) {
        return new SelfDefenseModelImpl(sdkManager, settings);
    }

    @Binds
    @NonNull
    abstract SelfDefenseModel provideSelfDefenseModel(@NonNull ManageableSelfDefenseModel model);

    @Provides
    @Singleton
    @NonNull
    static TabInfoFactoryAndroid provideTabInfoFactory() {
        return new TabInfoFactoryImpl();
    }

    @Provides
    @NonNull
    static Settings provideSettings(@NonNull Context context) {
        return new SettingsImpl(context);
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }
}
