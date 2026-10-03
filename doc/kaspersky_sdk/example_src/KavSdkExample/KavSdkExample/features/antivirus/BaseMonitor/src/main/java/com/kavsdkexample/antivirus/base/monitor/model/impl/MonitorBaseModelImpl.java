/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.model.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ThreadType;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.impl.AntivirusModelImpl;
import com.kavsdkexample.antivirus.base.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseManager;
import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseModel;
import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseModelObserver;
import com.kavsdkexample.antivirus.base.monitor.model.settings.MonitorBaseSettings;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;

public class MonitorBaseModelImpl<MANAGER     extends MonitorBaseManager,
                                  TASKFACTORY extends TasksFactory,
                                  SETTINGS    extends MonitorBaseSettings>
                          extends AntivirusModelImpl<TASKFACTORY, SETTINGS>
                          implements MonitorBaseModel,
                                     MonitorBaseManager.MonitorStateObserver {

    @NonNull
    private final MANAGER             mMonitorManager;

    public MonitorBaseModelImpl(@NonNull ExecutorService          executorService,
                                @NonNull MANAGER                  monitorManager,
                                @NonNull TASKFACTORY              tasksFactory,
                                @NonNull SETTINGS                 settings,
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

        mMonitorManager      = monitorManager;

        runAntivirusAction(() -> {
            monitorManager.init();
            afterInitMonitor();
            mMonitorManager.applyCheckRiskware(settings.getScanRiskware());
            mMonitorManager.applySuspiciousCheck(settings.getScanSuspicious());
            mMonitorManager.applyMaxFileCheckSize(settings.getMaxFileCheckSize());
            mMonitorManager.applyCloudCheck(settings.getAllowCloudScan());
            mMonitorManager.applyCloudOnlyCheck(settings.getCloudOnlyScan());
        }, ThreadType.Ui);
    }

    protected void afterInitMonitor() {
    }

    @Override
    public void onMonitorState(boolean enabled) {
        notifyObservers(observer -> observer.onMonitorStateChanged(enabled), MonitorBaseModelObserver.class);
    }

    @NonNull
    protected final MANAGER getMonitorManager() {
        return mMonitorManager;
    }

    protected final boolean isScanningAllowed() {
        return getSettings().getCloudOnlyScan() || getSdkManager().isBasesAvailable();
    }

    @Override
    public void setMonitorState(boolean enabled) {
        getSettings().setMonitorState(enabled);

        runAntivirusAction(() -> {
            boolean monitorState = getSettings().getMonitorState();

            if (monitorState && !isScanningAllowed()) {
                getSettings().setMonitorState(false);
                notifyBasesUnavailable();
                notifyObservers(observer -> observer.onMonitorStateChanged(false), MonitorBaseModelObserver.class);
            } else {
                mMonitorManager.applyMonitorState(monitorState, this);
            }
        }, ThreadType.Ui);
    }

    @Override
    public boolean getMonitorState() {
        return getSettings().getMonitorState();
    }

    @Override
    public void setMaxFileCheckSize(long size) {
        getSettings().setMaxFileCheckSize(size);
        runAntivirusAction(() -> mMonitorManager.applyMaxFileCheckSize(size), ThreadType.Ui);

    }

    @Override
    public void setCloudOnlyScan(boolean enabled) {
        super.setCloudOnlyScan(enabled);

        runAntivirusAction(() -> {
            if (isScanningAllowed() || !mMonitorManager.getMonitorState()) {
                mMonitorManager.applyCloudOnlyCheck(getSettings().getCloudOnlyScan());
            } else {
                getSettings().setCloudOnlyScan(true);
                notifyBasesUnavailable();
                notifyObservers(observer -> observer.onMonitorStateChanged(true), MonitorBaseModelObserver.class);
            }
        }, ThreadType.Ui);
    }

    @Override
    public void setAllowCloudScan(boolean enabled) {
        super.setAllowCloudScan(enabled);
        runAntivirusAction(() -> mMonitorManager.applyCloudCheck(enabled), ThreadType.Ui);
    }

    @Override
    public void setScanRiskware(boolean enabled) {
        super.setScanRiskware(enabled);
        runAntivirusAction(() -> mMonitorManager.applyCheckRiskware(enabled), ThreadType.Ui);
    }

    @Override
    public void setScanSuspicious(boolean enabled) {
        super.setScanSuspicious(enabled);
        runAntivirusAction(() -> mMonitorManager.applySuspiciousCheck(enabled), ThreadType.Ui);
    }

    @Override
    public long getMaxFileCheckSize() {
        return getSettings().getMaxFileCheckSize();
    }
}
