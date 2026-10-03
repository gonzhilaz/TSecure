/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.model.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ThreadType;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.base.monitor.model.impl.MonitorBaseModelImpl;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.antivirus.pua_monitor.model.PuaMonitorModel;
import com.kavsdkexample.antivirus.pua_monitor.sdk.PuaMonitorManager;

import com.kavsdkexample.antivirus.pua_monitor.model.settings.Settings;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.concurrent.ExecutorService;

public class PuaMonitorModelImpl extends    MonitorBaseModelImpl<PuaMonitorManager, TasksFactory, Settings>
                                 implements PuaMonitorModel {

    public PuaMonitorModelImpl(@NonNull ExecutorService          executorService,
                               @NonNull TasksFactory             tasksFactory,
                               @NonNull PuaMonitorManager        puaMonitorManager,
                               @NonNull Settings                 settings,
                               @NonNull ThreatInfoCollector      threatInfoCollector,
                               @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                               @NonNull ThreadManager            threadManager,
                               @NonNull SdkManager               sdkManager,
                               @NonNull ThreatHandler            threatHandler,
                               @NonNull PackageMonitor           packageMonitor,
                               @NonNull UiLauncher               uiLauncher) {
        super(executorService,
              puaMonitorManager,
              tasksFactory,
              settings,
              threatInfoCollector,
              threatInfoWrapperFactory,
              threadManager,
              sdkManager,
              threatHandler,
              packageMonitor,
              uiLauncher);

        runAntivirusAction(() -> {
            PuaMonitorManager monitorManager = getMonitorManager();
            monitorManager.applyProcessMissedApps(!getSettings().getMissedAppCheckState());
        }, ThreadType.Ui);
        setMonitorState(settings.getMonitorState());
    }

    @Override
    public void setMissedAppCheckState(boolean value) {
        getSettings().setMissedAppCheckState(value);
    }

    @Override
    public boolean getMissedAppCheckState() {
        return getSettings().getMissedAppCheckState();
    }
}
