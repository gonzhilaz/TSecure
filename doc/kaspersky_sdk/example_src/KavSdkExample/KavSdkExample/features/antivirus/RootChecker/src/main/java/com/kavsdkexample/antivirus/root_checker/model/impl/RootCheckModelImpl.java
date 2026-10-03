/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.model.impl;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.antivirus.base.model.ThreadType;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.impl.AntivirusModelImpl;
import com.kavsdkexample.antivirus.base.model.settings.AvFeatureSettings;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.antivirus.root_checker.model.RootCheckModel;
import com.kavsdkexample.antivirus.root_checker.model.RootCheckModelObserver;
import com.kavsdkexample.antivirus.root_checker.model.RootCheckResultsObserver;
import com.kavsdkexample.antivirus.root_checker.model.tasks.TasksFactory;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@UiThread
public class RootCheckModelImpl extends AntivirusModelImpl<TasksFactory, AvFeatureSettings>
                                implements RootCheckModel,
                                           RootCheckResultsObserver {
    private static final long LAST_ROOT_CHECK_RESULT_TTL = TimeUnit.SECONDS.toMillis(30);
    private long mLastRootCheckTimeMs;
    private boolean mIsRooted;

    public RootCheckModelImpl(@NonNull ExecutorService          executorService,
                              @NonNull TasksFactory             tasksFactory,
                              @NonNull AvFeatureSettings        settings,
                              @NonNull ThreatInfoCollector      threatInfoCollector,
                              @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                              @NonNull ThreadManager            threadManager,
                              @NonNull SdkManager               sdkManager,
                              @NonNull ThreatHandler            threatHandler,
                              @NonNull PackageMonitor           packageMonitor,
                              @NonNull UiLauncher               uiLauncher) {

        super(executorService,
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

    @Override
    public void checkRoot() {
        if (isCheckCompleted()) {
            notifyObservers(observer -> observer.onRootCheckResults(mIsRooted), RootCheckModelObserver.class);
            return;
        }
        runAntivirusAction(getTasksFactory().createRootCheckTask(getThreadManager(), this), ThreadType.Worker);
    }

    @Override
    public boolean isCheckCompleted() {
        long currentTimeMs = System.currentTimeMillis();
        return mLastRootCheckTimeMs != 0L &&
               Math.abs(currentTimeMs - mLastRootCheckTimeMs) <= LAST_ROOT_CHECK_RESULT_TTL;
    }

    @Override
    public boolean isRooted() {
        return mIsRooted;
    }

    @Override
    public void onSuccess(boolean isRooted) {
        mLastRootCheckTimeMs = System.currentTimeMillis();
        mIsRooted            = isRooted;
        notifyObservers(observer -> observer.onRootCheckResults(isRooted), RootCheckModelObserver.class);
    }

    @Override
    public void onFailed(@NonNull Exception e) {
        notifyObservers(observer -> observer.onError(e.getMessage()), RootCheckModelObserver.class);
    }

    @Override
    public void onBasesUnavailable() {
        notifyObservers(RootCheckModelObserver::onBasesUnavailable, RootCheckModelObserver.class);
    }
}
