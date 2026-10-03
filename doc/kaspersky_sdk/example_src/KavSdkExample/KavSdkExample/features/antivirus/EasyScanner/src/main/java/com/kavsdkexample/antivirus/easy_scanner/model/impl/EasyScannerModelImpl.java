/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.model.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ThreadType;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.impl.BaseScannerModelImpl;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanObserver;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerModel;
import com.kavsdkexample.antivirus.easy_scanner.model.settings.EasyScanSettings;
import com.kavsdkexample.antivirus.easy_scanner.model.tasks.TasksFactory;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;

public class EasyScannerModelImpl extends    BaseScannerModelImpl<TasksFactory, EasyScanResults, EasyScanSettings>
                                  implements EasyScannerModel,
                                             EasyScanObserver {

    public EasyScannerModelImpl(@NonNull ExecutorService          executorService,
                                @NonNull TasksFactory             tasksFactory,
                                @NonNull EasyScanSettings         settings,
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

    @NonNull
    @Override
    public EasyScannerMode getEasyScannerMode() {
        return getSettings().getEasyScannerMode();
    }

    @Override
    public void setEasyScannerMode(@NonNull EasyScannerMode mode) {
        getSettings().setEasyScannerMode(mode);
    }

    @Override
    public boolean startScan() {
        runAntivirusAction(
            getTasksFactory().createEasyScanTask(
                getThreatInfoCollector(),
                getThreatInfoWrapperFactory(),
                getThreatHandler(),
                getThreadManager(),
                getSettings(),
                this
            ),
            ThreadType.Worker
        );
        return super.startScan();
    }
}
