/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.model.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.impl.BaseScannerModelImpl;
import com.kavsdkexample.antivirus.pua_scanner.model.PuaScannerModel;
import com.kavsdkexample.antivirus.pua_scanner.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.base.model.ThreadType;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.settings.AvFeatureSettings;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;


public class PuaScanModelImpl extends    BaseScannerModelImpl<TasksFactory, ScanResults, AvFeatureSettings>
                              implements PuaScannerModel, ScanObserver<ScanResults> {

    public PuaScanModelImpl(@NonNull ExecutorService          executorService,
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
    public boolean startScan() {
        runAntivirusAction(
                getTasksFactory().createPuaScanTask(
                        getThreatInfoCollector(),
                        getThreatInfoWrapperFactory(),
                        getThreatHandler(),
                        getThreadManager(),
                        this
                ),
                ThreadType.Worker
        );
        return super.startScan();
    }
}
