/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.features.TabInfoFactory;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.TabInfoFactoryImpl;
import com.kavsdkexample.core.app.model.interactors.ManageableServiceInteractor;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerImpl;
import com.kavsdkexample.model.AppModel;
import com.kavsdkexample.model.SdkFeatureManager;
import com.kavsdkexample.model.SdkFeatureProvider;
import com.kavsdkexample.model.app.SdkInitializer;
import com.kavsdkexample.model.app.impl.AppModelImpl;
import com.kavsdkexample.model.app.impl.BaseApplication;
import com.kavsdkexample.model.app.impl.SdkFeatureManagerImpl;
import com.kavsdkexample.model.app.permissions.PermissionRepository;
import com.kavsdkexample.model.app.permissions.impl.PermissionRepositoryImpl;
import com.kavsdkexample.model.app.sdk.SdkWrappersFactory;
import com.kavsdkexample.model.app.sdk.impl.SdkWrappersFactoryImpl;
import com.kavsdkexample.model.app.settings.Settings;
import com.kavsdkexample.model.app.settings.impl.SettingsImpl;
import com.kavsdkexample.model.app.tasks.TasksFactory;
import com.kavsdkexample.model.app.tasks.impl.TasksFactoryImpl;
import com.kavsdkexample.core.app.model.interactors.NotificationInteractor;
import com.kavsdkexample.model.interactors.impl.NotificationInteractorImpl;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.model.interactors.impl.ServiceInteractorImpl;

import java.util.concurrent.ExecutorService;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
abstract class AppModule {
    @Provides
    @NonNull
    static Context provideContext(Application application) {
        return application;
    }

    @Provides
    @NonNull
    static ExecutorService provideExecutorService() {
        return AppThreadPool.getExecutorService();
    }

    @Provides
    @Singleton
    @NonNull
    static AppModel provideAppModel(@NonNull Settings               settings,
                                    @NonNull SdkFeatureProvider     featureProvider,
                                    @NonNull PermissionRepository   permissionRepository,
                                    @NonNull ThreadManager          threadManager,
                                    @NonNull ExecutorService        executor,
                                    @NonNull TasksFactory           tasksFactory,
                                    @NonNull SdkWrappersFactory     sdkWrappersFactory,
                                    @NonNull TabInfoFactory         tabInfoFactory,
                                    @NonNull ServiceInteractor      serviceInteractor,
                                    @NonNull NotificationInteractor notificationInteractor) {
        return new AppModelImpl(settings,
                                featureProvider,
                                permissionRepository,
                                threadManager,
                                executor,
                                tasksFactory,
                                sdkWrappersFactory,
                                tabInfoFactory,
                                serviceInteractor,
                                notificationInteractor);
    }

    @Provides
    @Singleton
    @NonNull
    static SdkFeatureManager provideSdkFeatureManager(@NonNull Application            app,
                                                      @NonNull ThreadManager          threadManager,
                                                      @NonNull ServiceInteractor      serviceInteractor,
                                                      @NonNull NotificationInteractor notificationInteractor) {
        return new SdkFeatureManagerImpl(app, threadManager, serviceInteractor, notificationInteractor);
    }

    @Binds
    @NonNull
    abstract SdkFeatureProvider bindFeatureProvider(@NonNull SdkFeatureManager manager);

    @Provides
    @Singleton
    @NonNull
    static Settings provideSettings(@NonNull Application application) {
        return new SettingsImpl(application.getApplicationContext());
    }

    @Provides
    @Singleton
    @NonNull
    static TasksFactory provideTasksFactory(@NonNull Context context) {
        return new TasksFactoryImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static PermissionRepository providePermissionRepository(@NonNull Context context) {
        return new PermissionRepositoryImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static TabInfoFactoryAndroid provideTabInfoFactoryAndroid() {
        return new TabInfoFactoryImpl();
    }

    @Binds
    @NonNull
    abstract TabInfoFactory bindTabInfoFactory(@NonNull TabInfoFactoryAndroid factory);

    @Provides
    @Singleton
    @NonNull
    static ManageableServiceInteractor provideManageableServiceInteractorFactory(@NonNull Context context) {
        return new ServiceInteractorImpl(context);
    }

    @Binds
    @NonNull
    abstract ServiceInteractor bindServiceInteractor(@NonNull ManageableServiceInteractor serviceInteractor);

    @Provides
    @Singleton
    @NonNull
    static ThreadManager provideThreadManager() {
        return new ThreadManagerImpl();
    }

    @Provides
    @NonNull
    static SdkWrappersFactory provideSdkWrappersFactory(@NonNull Context context) {
        return new SdkWrappersFactoryImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static NotificationInteractor provideNotificationInteractor(@NonNull Context context) {
        return new NotificationInteractorImpl(context);
    }

    @Binds
    @NonNull
    abstract Application bindApp(BaseApplication app);

    @Binds
    @NonNull
    abstract SdkInitializer bindSdkInitializer(AppModel model);

}
