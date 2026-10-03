/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.model.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.model.ThreadType;
import com.kavsdkexample.antivirus.base.model.ThreatHandler;
import com.kavsdkexample.antivirus.base.model.ThreatInfoWrapperFactory;
import com.kavsdkexample.antivirus.base.model.tasks.TasksFactory;
import com.kavsdkexample.antivirus.base.monitor.model.impl.MonitorBaseModelImpl;
import com.kavsdkexample.antivirus.base.repository.storage.ThreatInfoCollector;
import com.kavsdkexample.antivirus.base.repository.system.PackageMonitor;
import com.kavsdkexample.antivirus.base.sdk.SdkManager;
import com.kavsdkexample.antivirus.base.view.UiLauncher;
import com.kavsdkexample.antivirus.rtp_monitor.model.RtpModelObserver;
import com.kavsdkexample.antivirus.rtp_monitor.model.RtpMonitorModel;
import com.kavsdkexample.antivirus.rtp_monitor.model.settings.Settings;
import com.kavsdkexample.antivirus.rtp_monitor.sdk.RtpMonitorManager;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class RtpMonitorModelImpl extends    MonitorBaseModelImpl<RtpMonitorManager, TasksFactory, Settings>
                                 implements RtpMonitorModel {

    private Map<String, Integer> mFoldersToMonitor;
    private Set<String>          mExcludedFolders;

    public RtpMonitorModelImpl(@NonNull ExecutorService executorService,
                               @NonNull TasksFactory             tasksFactory,
                               @NonNull RtpMonitorManager        rtpMonitorManager,
                               @NonNull Settings                 settings,
                               @NonNull ThreatInfoCollector      threatInfoCollector,
                               @NonNull ThreatInfoWrapperFactory threatInfoWrapperFactory,
                               @NonNull ThreadManager            threadManager,
                               @NonNull SdkManager               sdkManager,
                               @NonNull ThreatHandler            threatHandler,
                               @NonNull PackageMonitor           packageMonitor,
                               @NonNull UiLauncher               uiLauncher) {
        super(executorService,
              rtpMonitorManager,
              tasksFactory,
              settings,
              threatInfoCollector,
              threatInfoWrapperFactory,
              threadManager,
              sdkManager,
              threatHandler,
              packageMonitor,
              uiLauncher);

        mFoldersToMonitor = new HashMap<>();
        mExcludedFolders  = new HashSet<>();

        runAntivirusAction(() -> {
            RtpMonitorManager monitorManager = getMonitorManager();
            Map<String, Integer> folders = settings.getFoldersToMonitor();
            if (folders == null) {
                mFoldersToMonitor = monitorManager.getFoldersToMonitor();
            } else {
                mFoldersToMonitor = folders;
                monitorManager.setFoldersToMonitor(folders);
            }
            Set<String> excludeFolders = settings.getFoldersToExclude();
            if (excludeFolders == null) {
                mExcludedFolders = monitorManager.getExcludeFolders();
            } else {
                mExcludedFolders = excludeFolders;
                monitorManager.setExcludeFolders(excludeFolders);
            }
            monitorManager.setAvAction(settings.getAvAction());
        }, ThreadType.Ui);
        setMonitorState(settings.getMonitorState());
    }

    @Override
    public void addFolderForMonitoring(@NonNull String folder, int flags) {
        runAntivirusAction(() -> {
            @Nullable
            Integer prevValue = mFoldersToMonitor.put(folder, flags);

            if (prevValue == null || flags != prevValue) {
                getSettings().setFoldersToMonitor(mFoldersToMonitor);
                getMonitorManager().addFolderToMonitor(folder, flags);
                notifyObservers(observer -> observer.onMonitorFolderAdded(folder), RtpModelObserver.class);
            }
        }, ThreadType.Ui);
    }

    @Override
    public void removeFolderFromMonitoring(@NonNull String folder) {
        runAntivirusAction(() -> {
            if (mFoldersToMonitor.remove(folder) != null) {
                notifyObservers(observer -> observer.onMonitorFolderRemoved(folder), RtpModelObserver.class);
                getSettings().setFoldersToMonitor(mFoldersToMonitor);
                getMonitorManager().removeFolderFromMonitor(folder);
            }
        }, ThreadType.Ui);
    }

    @Override
    public void addExcludedFolder(@NonNull String folder) {
        runAntivirusAction(() -> {
            if (mExcludedFolders.add(folder)) {
                getSettings().setFoldersToExclude(mExcludedFolders);
                getMonitorManager().addExcludeFolder(folder);
                notifyObservers(observer -> observer.onExclusionFolderAdded(folder), RtpModelObserver.class);
            }
        }, ThreadType.Ui);
    }

    @Override
    public void removeExcludedFolder(@NonNull String folder) {
        runAntivirusAction(() -> {
            if (mExcludedFolders.remove(folder)) {
                getSettings().setFoldersToExclude(mExcludedFolders);
                getMonitorManager().removeExcludeFolder(folder);
                notifyObservers(observer -> observer.onExclusionRemoved(folder), RtpModelObserver.class);
            }
        }, ThreadType.Ui);
    }

    @Override
    public void addDefaultDirectories() {
        runAntivirusAction(() -> {
            getMonitorManager().addDefaultDirectories();
            final Map<String, Integer> folders = getMonitorManager().getFoldersToMonitor();
            getSettings().setFoldersToMonitor(mFoldersToMonitor);
            mFoldersToMonitor = folders;
            notifyObservers(observer -> observer.onMonitorFoldersChanged(folders), RtpModelObserver.class);
        }, ThreadType.Ui);
    }

    @Override
    public void removeDefaultDirectories() {
        runAntivirusAction(() -> {
            getMonitorManager().removeDefaultDirectories();
            final Map<String, Integer> folders = getMonitorManager().getFoldersToMonitor();
            getSettings().setFoldersToMonitor(mFoldersToMonitor);
            mFoldersToMonitor = folders;
            notifyObservers(observer -> observer.onMonitorFoldersChanged(folders), RtpModelObserver.class);
        }, ThreadType.Ui);
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        super.setAvAction(action);
        runAntivirusAction(() -> getMonitorManager().setAvAction(action), ThreadType.Ui);
    }

    @Override
    @NonNull
    public Map<String, Integer> getMonitoredFolders() {
        return mFoldersToMonitor;
    }

    @Override
    @NonNull
    public Set<String> getExcludedFolders() {
        return mExcludedFolders;
    }
}
