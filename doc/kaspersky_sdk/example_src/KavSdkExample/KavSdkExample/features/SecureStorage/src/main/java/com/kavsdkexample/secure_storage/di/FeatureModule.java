/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;
import com.kavsdkexample.secure_storage.model.database.DbModel;
import com.kavsdkexample.secure_storage.model.database.DbTasksFactory;
import com.kavsdkexample.secure_storage.model.database.impl.DbModelImpl;
import com.kavsdkexample.secure_storage.sdk.impl.DbTasksFactoryImpl;
import com.kavsdkexample.secure_storage.model.file.FileModel;
import com.kavsdkexample.secure_storage.model.file.FileTasksFactory;
import com.kavsdkexample.secure_storage.model.file.impl.FileModelImpl;
import com.kavsdkexample.secure_storage.model.file.impl.FileTasksFactoryImpl;

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
    static DbTasksFactory provideDbTasksFactory(@NonNull Context context) {
        return new DbTasksFactoryImpl(context);
    }

    @Provides
    @NonNull
    static FileTasksFactory provideFileTasksFactory(@NonNull Context context) {
        return new FileTasksFactoryImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static DbModel provideDbModel(@NonNull ThreadManager   threadManager,
                                  @NonNull ExecutorService executorService,
                                  @NonNull DbTasksFactory  tasksFactory) {
        return new DbModelImpl(threadManager, executorService, tasksFactory);
    }

    @Provides
    @Singleton
    @NonNull
    static FileModel provideFileModel(@NonNull ThreadManager    threadManager,
                                      @NonNull ExecutorService  executorService,
                                      @NonNull FileTasksFactory tasksFactory) {
        return new FileModelImpl(threadManager, executorService, tasksFactory);
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }
}
