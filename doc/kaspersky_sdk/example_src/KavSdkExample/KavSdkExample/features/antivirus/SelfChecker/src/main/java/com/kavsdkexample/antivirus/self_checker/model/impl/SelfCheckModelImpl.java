/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.model.impl;

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
import com.kavsdkexample.antivirus.self_checker.model.SelfCheckModel;
import com.kavsdkexample.antivirus.self_checker.model.SelfCheckModelObserver;
import com.kavsdkexample.antivirus.self_checker.model.SelfCheckResultsObserver;
import com.kavsdkexample.antivirus.self_checker.model.tasks.TasksFactory;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@UiThread
public class SelfCheckModelImpl extends AntivirusModelImpl<TasksFactory, AvFeatureSettings>
                                 implements SelfCheckModel,
                                            SelfCheckResultsObserver {
    private static final long LAST_SELF_CHECK_RESULT_TTL = TimeUnit.SECONDS.toMillis(30);
    private long mLastSelfCheckTimeMs;
    private boolean mIsCompromised;

    public SelfCheckModelImpl(@NonNull ExecutorService          executorService,
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
    public void checkSelf() {
        if (isCheckCompleted()) {
            notifyObservers(observer -> observer.onSelfCheckResults(mIsCompromised), SelfCheckModelObserver.class);
            return;
        }
        runAntivirusAction(getTasksFactory().createSelfCheckTask(getThreadManager(), this), ThreadType.Worker);
    }

    @Override
    public boolean isCheckCompleted() {
        long currentTimeMs = System.currentTimeMillis();
        return mLastSelfCheckTimeMs != 0L &&
                Math.abs(currentTimeMs - mLastSelfCheckTimeMs) <= LAST_SELF_CHECK_RESULT_TTL;
    }

    @Override
    public boolean isCompromised() {
        return mIsCompromised;
    }

    @Override
    public void onSuccess(boolean isCompromised) {
        mLastSelfCheckTimeMs = System.currentTimeMillis();
        mIsCompromised       = isCompromised;
        notifyObservers(observer -> observer.onSelfCheckResults(isCompromised), SelfCheckModelObserver.class);
    }

    @Override
    public void onBasesUnavailable() {
        notifyObservers(SelfCheckModelObserver::onBasesUnavailable, SelfCheckModelObserver.class);
    }
}
