/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.di;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.pua_scanner.model.PuaScannerModel;
import com.kavsdkexample.antivirus.pua_scanner.model.impl.PuaScanModelImpl;
import com.kavsdkexample.antivirus.pua_scanner.model.settings.impl.PuaScanSettingsImpl;
import com.kavsdkexample.antivirus.pua_scanner.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.pua_scanner.sdk.TasksFactoryImpl;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.impl.ThreatInfoWrapperFactoryImpl;
import com.kavsdkexample.antivirus.base.model.settings.AvFeatureSettings;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoProvider;
import com.kavsdkexample.antivirus.base.repository.storage.impl.ScanInfoStorage;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.repository.system.impl.PackageMonitorInstance;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.sdk.impl.SdkManagerImpl;
import com.kavsdkexample.antivirus.base.sdk.impl.ThreatHandlerImpl;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.antivirus.base.view.impl.UiLauncherImpl;
import com.kavsdkexample.core.app.utils.AppThreadPool;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.core.app.utils.impl.ThreadManagerInstance;

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
    static ThreatHandler provideThreatHandler() {
        return new ThreatHandlerImpl();
    }

    @Provides
    @Singleton
    @NonNull
    static PuaScannerModel providePuaScanModel(@NonNull ExecutorService          executorService,
                                               @NonNull TasksFactory             tasksFactory,
                                               @NonNull AvFeatureSettings        settings,
                                               @NonNull ThreadManager            threadManager,
                                               @NonNull ThreatInfoCollector      threatInfoCollector,
                                               @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                                               @NonNull SdkManager               sdkManager,
                                               @NonNull ThreatHandler            threatHandler,
                                               @NonNull PackageMonitor           packageMonitor,
                                               @NonNull UiLauncher               uiLauncher) {
        return new PuaScanModelImpl(
                executorService,
                tasksFactory,
                settings,
                threatInfoCollector,
                threatInfoWrapperFactory,
                threadManager,
                sdkManager,
                threatHandler,
                packageMonitor,
                uiLauncher);
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
    static AvFeatureSettings provideSettings() {
        return new PuaScanSettingsImpl();
    }

    @Provides
    @Singleton
    @NonNull
    static PackageMonitor providePackageMonitor(@NonNull Context context) {
        return PackageMonitorInstance.getInstance(context);
    }

    @Provides
    @NonNull
    static ThreatInfoCollector provideThreatCollector(@NonNull Context context) {
        return ScanInfoStorage.getInstance(context);
    }

    @Provides
    @NonNull
    static ThreatInfoProvider provideThreatInfoProvide(@NonNull Context context) {
        return ScanInfoStorage.getInstance(context);
    }

    @Provides
    @NonNull
    static SdkManager provideSdkManager() {
        return new SdkManagerImpl();
    }

    @Provides
    @NonNull
    static ThreatInfoWrapperFactory provideThreatInfoWrapperFactory() {
        return new ThreatInfoWrapperFactoryImpl();
    }

    @Provides
    @NonNull
    static ThreadManager provideThreadManager() {
        return ThreadManagerInstance.getInstance();
    }

    @Provides
    @Singleton
    @NonNull
    static UiLauncher provideUiLauncher(@NonNull Context context) {
        return new UiLauncherImpl(context);
    }
}
