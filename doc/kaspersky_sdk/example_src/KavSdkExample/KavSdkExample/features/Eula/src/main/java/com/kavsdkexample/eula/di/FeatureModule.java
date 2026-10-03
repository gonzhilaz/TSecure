/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.TabInfoFactoryImpl;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;
import com.kavsdkexample.eula.model.EulaManageableModel;
import com.kavsdkexample.eula.model.EulaModel;
import com.kavsdkexample.eula.model.impl.EulaModelImpl;
import com.kavsdkexample.eula.model.settings.Settings;
import com.kavsdkexample.eula.model.settings.impl.SettingsImpl;
import com.kavsdkexample.eula.model.tasks.EulaTasksFactory;
import com.kavsdkexample.eula.model.tasks.impl.EulaTasksFactoryImpl;

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
    @NonNull
    static EulaTasksFactory provideEulaTaskFactory(@NonNull Context context) {
        return new EulaTasksFactoryImpl(context);
    }

    @Provides
    @NonNull
    static Settings provideSettings(@NonNull Context context) {
        return new SettingsImpl(context);
    }

    @Binds
    @NonNull
    abstract EulaModel provideEulaModel(@NonNull EulaManageableModel manageableModel);

    @Provides
    @Singleton
    @NonNull
    static EulaManageableModel provideEulaManageableModel(@NonNull ThreadManager    threadManager,
                                                          @NonNull ExecutorService  executorService,
                                                          @NonNull EulaTasksFactory factory,
                                                          @NonNull Settings         settings) {
        return new EulaModelImpl(threadManager, executorService, factory, settings);
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
