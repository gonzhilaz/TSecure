/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.antiphishing.model.WebFilterModel;
import com.kavsdkexample.antiphishing.model.impl.WebFilterModelImpl;
import com.kavsdkexample.antiphishing.model.tasks.TasksFactory;
import com.kavsdkexample.antiphishing.repository.sdk.WebFilterManager;
import com.kavsdkexample.antiphishing.repository.sdk.impl.TasksFactoryImpl;
import com.kavsdkexample.antiphishing.repository.sdk.impl.WebFilterManagerImpl;
import com.kavsdkexample.antiphishing.repository.settings.Settings;
import com.kavsdkexample.antiphishing.repository.settings.impl.SettingsImpl;
import com.kavsdkexample.antiphishing.repository.sdk.SdkLocalStatusObserver;
import com.kavsdkexample.antiphishing.repository.sdk.impl.SdkLocalStatusObserverImpl;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;

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
    @Singleton
    @NonNull
    static WebFilterModel provideWebFilterModel(@NonNull ThreadManager             threadManager,
                                                @NonNull ExecutorService           executorService,
                                                @NonNull TasksFactory              tasksFactory,
                                                @NonNull WebFilterManager          webFilterManager,
                                                @NonNull Settings                  settings) {
        return new WebFilterModelImpl(threadManager, executorService, tasksFactory, webFilterManager, settings);
    }

    @Provides
    @NonNull
    static ExecutorService provideExecutorService() {
        return AppThreadPool.getExecutorService();
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }

    @Provides
    @Singleton
    @NonNull
    static WebFilterManagerImpl provideWebFilterManager(@NonNull Context  context,
                                                        @NonNull Settings settings) {
        return new WebFilterManagerImpl(context, settings);
    }

    @Binds
    @NonNull
    abstract WebFilterManager bindWebFilterManager(@NonNull WebFilterManagerImpl manager);

    @Provides
    @Singleton
    @NonNull
    static TasksFactory provideTasksFactory(@NonNull Context context, @NonNull WebFilterManagerImpl webFilterManager) {
        return new TasksFactoryImpl(context, webFilterManager);
    }

    @Provides
    @Singleton
    @NonNull
    static Settings provideSettings(@NonNull Context context) {
        return new SettingsImpl(context);
    }


    @Provides
    @Singleton
    @NonNull
    static SdkLocalStatusObserver provideSdkLocalStatusObserver(@NonNull WebFilterModel webFilterModel, @NonNull ThreadManager threadManager) {
        return new SdkLocalStatusObserverImpl(webFilterModel, threadManager);
    }
}
