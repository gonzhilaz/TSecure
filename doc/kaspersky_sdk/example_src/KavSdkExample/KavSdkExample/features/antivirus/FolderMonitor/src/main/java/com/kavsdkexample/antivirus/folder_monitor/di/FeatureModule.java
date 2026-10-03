/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.di;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.impl.ThreatInfoWrapperFactoryImpl;
import com.kavsdkexample.antivirus.base.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoProvider;
import com.kavsdkexample.antivirus.base.repository.storage.impl.ScanInfoStorage;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.repository.system.impl.PackageMonitorInstance;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.sdk.impl.SdkManagerImpl;
import com.kavsdkexample.antivirus.base.sdk.impl.TasksFactoryImpl;
import com.kavsdkexample.antivirus.base.sdk.impl.ThreatHandlerImpl;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.antivirus.base.view.impl.UiLauncherImpl;
import com.kavsdkexample.antivirus.folder_monitor.model.FolderMonitorModel;
import com.kavsdkexample.antivirus.folder_monitor.model.impl.FolderMonitorModelImpl;
import com.kavsdkexample.antivirus.folder_monitor.model.settings.Settings;
import com.kavsdkexample.antivirus.folder_monitor.model.settings.impl.SettingsImpl;
import com.kavsdkexample.antivirus.folder_monitor.sdk.FolderMonitorManager;
import com.kavsdkexample.antivirus.folder_monitor.sdk.impl.FolderMonitorManagerImpl;
import com.kavsdkexample.core.ui.UiProvider;
import com.kavsdkexample.core.ui.UiProviderImpl;
import com.kavsdkexample.core.app.features.TabInfoFactoryAndroid;
import com.kavsdkexample.core.app.features.impl.TabInfoFactoryImpl;
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
    static Settings provideSettings(@NonNull Context context) {
        return new SettingsImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static PackageMonitor providePackageMonitor(@NonNull Context context) {
        return PackageMonitorInstance.getInstance(context);
    }

    @Provides
    @Singleton
    @NonNull
    static TasksFactory provideTasksFactory(@NonNull Context context) {
        return new TasksFactoryImpl(context);
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
    static ThreatHandler provideThreatHandler() {
        return new ThreatHandlerImpl();
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
    static UiLauncher provideUiLauncher(@NonNull Context context) {
        return new UiLauncherImpl(context);
    }

    @Provides
    @Singleton
    @NonNull
    static FolderMonitorManager provideFolderMonitorManager(@NonNull final Context         context,
                                                            @NonNull final ThreadManager   threadManager,
                                                            @NonNull final ExecutorService executorService,
                                                            @NonNull final UiProvider      uiProvider) {
        return new FolderMonitorManagerImpl(context, threadManager, executorService, uiProvider);
    }

    @Provides
    @Singleton
    @NonNull
    static FolderMonitorModel provideFolderMonitorModel(@NonNull ExecutorService          executorService,
                                                        @NonNull TasksFactory             tasksFactory,
                                                        @NonNull FolderMonitorManager     folderMonitorManager,
                                                        @NonNull Settings                 settings,
                                                        @NonNull ThreadManager            threadManager,
                                                        @NonNull ThreatInfoCollector      threatInfoCollector,
                                                        @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                                                        @NonNull SdkManager               sdkManager,
                                                        @NonNull ThreatHandler            threatHandler,
                                                        @NonNull PackageMonitor           packageMonitor,
                                                        @NonNull UiLauncher               uiLauncher) {
        return new FolderMonitorModelImpl(
                executorService,
                tasksFactory,
                folderMonitorManager,
                settings,
                threatInfoCollector,
                threatInfoWrapperFactory,
                threadManager,
                sdkManager,
                threatHandler,
                packageMonitor,
                uiLauncher
        );
    }
}
