/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.update.model.UpdateModel;
import com.kavsdkexample.update.model.UpdateTaskFactory;
import com.kavsdkexample.update.model.impl.UpdateModelImpl;
import com.kavsdkexample.update.model.Settings;
import com.kavsdkexample.update.model.impl.SettingsImpl;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.TabInfoFactoryImpl;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;
import com.kavsdkexample.update.sdk.impl.UpdateTaskFactoryImpl;

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
    static UpdateTaskFactory provideUpdateTaskFactory() {
        return new UpdateTaskFactoryImpl();
    }

    @Provides
    @NonNull
    static Settings provideSettings(@NonNull Context context) {
        return new SettingsImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static UpdateModel provideUpdateModel(@NonNull ThreadManager threadManager,
                                          @NonNull UpdateTaskFactory taskFactory,
                                          @NonNull ExecutorService executorService,
                                          @NonNull Settings settings) {
        return new UpdateModelImpl(threadManager, taskFactory, executorService, settings);
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
}
