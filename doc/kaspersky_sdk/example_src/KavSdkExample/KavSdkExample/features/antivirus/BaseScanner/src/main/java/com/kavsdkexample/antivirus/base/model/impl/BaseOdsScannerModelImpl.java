/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.BaseOdsScannerModel;
import com.kavsdkexample.antivirus.base.model.ScanObjectsType;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.settings.BaseOdsSettings;
import com.kavsdkexample.antivirus.base.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class BaseOdsScannerModelImpl<TASKFACTORY extends TasksFactory,
                                              SCANRESULTS extends ScanResults,
                                              SETTINGS    extends BaseOdsSettings>
                             extends    BaseScannerModelImpl<TASKFACTORY, SCANRESULTS, SETTINGS>
                             implements BaseOdsScannerModel<SCANRESULTS> {


    public BaseOdsScannerModelImpl(@NonNull ExecutorService executorService,
                                   @NonNull TASKFACTORY tasksFactory,
                                   @NonNull SETTINGS    settings,
                                   @NonNull ThreatInfoCollector threatInfoCollector,
                                   @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                                   @NonNull ThreadManager  threadManager,
                                   @NonNull SdkManager     sdkManager,
                                   @NonNull ThreatHandler  threatHandler,
                                   @NonNull PackageMonitor packageMonitor,
                                   @NonNull UiLauncher     uiLauncher) {
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
    public void setObjectsToScan(@NonNull ScanObjectsType objectsToScan, @NonNull String path) {
        getSettings().setObjectsToScan(objectsToScan, path);
    }

    @NonNull
    @Override
    public ScanObjectsType getObjectsToScanType() {
        return getSettings().getObjectsToScanType();
    }

    @NonNull
    @Override
    public String getObjectsToScanPath() {
        return getSettings().getObjectsToScanPath();
    }

    public String getRootUri() {
        return getSettings().getRootUri();
    }

    public void setRootUri(@NonNull String rootUri) {
        getSettings().setRootUri(rootUri);
    }

    @NonNull
    @Override
    public List<String> getSdCardsOptions() {
        return getSdkManager().getStoragePaths(Integer.MAX_VALUE);
    }
}
