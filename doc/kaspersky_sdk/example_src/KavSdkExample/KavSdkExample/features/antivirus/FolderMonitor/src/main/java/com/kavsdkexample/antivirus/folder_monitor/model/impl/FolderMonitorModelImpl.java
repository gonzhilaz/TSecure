/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.model.impl;

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
import com.kavsdkexample.antivirus.folder_monitor.model.FolderMonitorModel;
import com.kavsdkexample.antivirus.folder_monitor.model.FolderMonitorModelObserver;
import com.kavsdkexample.antivirus.folder_monitor.model.settings.Settings;
import com.kavsdkexample.antivirus.folder_monitor.sdk.FolderMonitorManager;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class FolderMonitorModelImpl extends    MonitorBaseModelImpl<FolderMonitorManager, TasksFactory, Settings>
                                    implements FolderMonitorModel {

    public FolderMonitorModelImpl(@NonNull ExecutorService executorService,
                                  @NonNull TasksFactory             tasksFactory,
                                  @NonNull FolderMonitorManager     folderMonitorManager,
                                  @NonNull Settings                 settings,
                                  @NonNull ThreatInfoCollector      threatInfoCollector,
                                  @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                                  @NonNull ThreadManager            threadManager,
                                  @NonNull SdkManager               sdkManager,
                                  @NonNull ThreatHandler            threatHandler,
                                  @NonNull PackageMonitor           packageMonitor,
                                  @NonNull UiLauncher               uiLauncher) {
        super(executorService,
              folderMonitorManager,
              tasksFactory,
              settings,
              threatInfoCollector,
              threatInfoWrapperFactory,
              threadManager,
              sdkManager,
              threatHandler,
              packageMonitor,
              uiLauncher);
        setMonitorState(settings.getMonitorState());
    }

    @Override
    protected void afterInitMonitor() {
        Settings settings = getSettings();
        FolderMonitorManager monitorManager = getMonitorManager();
        Set<String> folders = settings.getFoldersToMonitor();
        if (folders == null) {
            settings.setMonitorState(false);
        } else {
            settings.setMonitorState(true);
            monitorManager.setFoldersToMonitor(folders);
        }
        monitorManager.applyCureInfected(settings.getTryCure());
    }

    @Override
    public void removeAllFolders() {
        runAntivirusAction(() -> {
            getMonitorManager().removeAllFolders();
            getSettings().setMonitorState(false);
            notifyObservers(FolderMonitorModelObserver::onMonitorFolderCleared, FolderMonitorModelObserver.class);
        }, ThreadType.Ui);
    }

    @Override
    public void addFolderForMonitoring(@NonNull String folder) {
        runAntivirusAction(() -> {
            Settings settings = getSettings();
            if (isScanningAllowed()) {
                FolderMonitorManager monitorManager = getMonitorManager();
                monitorManager.addFolderToMonitor(folder);
                settings.setFoldersToMonitor(monitorManager.getFoldersToMonitor());
                settings.setMonitorState(true);
                notifyObservers(observer -> observer.onMonitorFolderAdded(folder), FolderMonitorModelObserver.class);
            } else {
                notifyBasesUnavailable();
            }
        }, ThreadType.Ui);
    }

    @Override
    public void removeFolderFromMonitoring(@NonNull String folder) {
        runAntivirusAction(() -> {
            Settings settings = getSettings();
            FolderMonitorManager monitorManager = getMonitorManager();
            monitorManager.removeFolderFromMonitor(folder);
            settings.setFoldersToMonitor(monitorManager.getFoldersToMonitor());

            if (!monitorManager.getMonitorState()) {
                settings.setMonitorState(false);
            }

            notifyObservers(observer -> observer.onMonitorFolderRemoved(folder), FolderMonitorModelObserver.class);
        }, ThreadType.Ui);
    }


    @Override
    @NonNull
    public Set<String> getMonitoredFolders() {
        Set<String> folders = getSettings().getFoldersToMonitor();
        return folders == null ? new HashSet<>(0) : folders;
    }
}
