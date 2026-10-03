/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.sdk.impl;

import androidx.annotation.NonNull;
import android.util.Log;

import com.kavsdk.antivirus.Antivirus;
import com.kavsdk.antivirus.AntivirusInstance;
import com.kavsdk.antivirus.MonitorConstants;
import com.kavsdk.antivirus.MonitorEventListener;
import com.kavsdk.antivirus.MonitorItem;
import com.kavsdk.antivirus.MonitorSuspiciousEventListener;
import com.kavsdk.antivirus.QuarantineException;
import com.kavsdk.antivirus.SuspiciousThreatType;
import com.kavsdk.antivirus.ThreatInfo;
import com.kavsdk.antivirus.ThreatType;
import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.sdk.impl.Utils;
import com.kavsdkexample.antivirus.rtp_monitor.sdk.RtpMonitorManager;
import com.kavsdkexample.core.app.utils.ThreadManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class RtpMonitorManagerImpl implements RtpMonitorManager,
                                              MonitorEventListener,
                                              MonitorSuspiciousEventListener {
    private final static String TAG                    = RtpMonitorManagerImpl.class.getSimpleName();
    private static final String MONITOR_EVENT_PATH_MSG = "Monitor event: path = ";

    private final ThreadManager   mThreadManager;
    private final ExecutorService mExecutorService;
    private AvAction mAvAction = AvAction.SkipThreat;

    public RtpMonitorManagerImpl(@NonNull ThreadManager threadManager,
                                 @NonNull ExecutorService executorService) {
        mThreadManager   = threadManager;
        mExecutorService = executorService;
    }

    @Override
    public void init() {
        Antivirus av = AntivirusInstance.getInstance();
        av.setMonitorListener(this);
        av.setMonitorSuspiciousListener(this);
        av.setMonitorCleanMode(MonitorConstants.LOG_ONLY);
    }

    @Override
    public void applyMonitorState(boolean enabled, @NonNull MonitorStateObserver observer) {
        mExecutorService.execute(() -> {
            final Antivirus av = AntivirusInstance.getInstance();
            try {
                av.setMonitorState(enabled);
            } catch (SdkLicenseViolationException e) {
                Log.e(TAG, "Failed to init RTP monitor - license error: " + e.getMessage());
            }
            mThreadManager.runOnUiThread(() -> observer.onMonitorState(av.isMonitorActive()));
        });

    }

    @Override
    public boolean getMonitorState() {
        return AntivirusInstance.getInstance().isMonitorActive();
    }

    @Override
    public void applyCloudOnlyCheck(boolean enabled) {
        Antivirus av    = AntivirusInstance.getInstance();
        int currentMode = av.getMonitorScanMode();
        currentMode     = enabled ? currentMode | MonitorConstants.ONLY_UDS : currentMode & ~MonitorConstants.ONLY_UDS;
        av.setMonitorScanMode(currentMode);
    }

    @Override
    public void applyCloudCheck(boolean enabled) {
        Antivirus av    = AntivirusInstance.getInstance();
        int currentMode = av.getMonitorScanMode();
        currentMode     = enabled ? currentMode | MonitorConstants.ALLOW_UDS : currentMode & ~MonitorConstants.ALLOW_UDS;
        av.setMonitorScanMode(currentMode);
    }

    @Override
    public void applyCheckRiskware(boolean enabled) {
        Antivirus av    = AntivirusInstance.getInstance();
        int currentMode = av.getMonitorScanMode();
        currentMode     = enabled
                ? currentMode | MonitorConstants.DETECT_RISKWARE_ADWARE
                : currentMode & ~MonitorConstants.DETECT_RISKWARE_ADWARE;
        av.setMonitorScanMode(currentMode);
    }

    @Override
    public void applySuspiciousCheck(boolean enabled) {
        Antivirus av    = AntivirusInstance.getInstance();
        int currentMode = av.getMonitorScanMode();
        currentMode     = enabled
                ? currentMode | MonitorConstants.DETECT_SUSPICIOUS
                : currentMode & ~MonitorConstants.DETECT_SUSPICIOUS;
        av.setMonitorScanMode(currentMode);
    }

    @Override
    public void applyMaxFileCheckSize(long size) {
        AntivirusInstance.getInstance().setMonitorMaxFileSize(size);
    }

    @Override
    public void addFolderToMonitor(@NonNull String folder, int flags) {
        AntivirusInstance.getInstance().addDirectoryToMonitor(folder, flags);
    }

    @Override
    public void setFoldersToMonitor(@NonNull Map<String, Integer> rtpItems) {
        Antivirus av            = AntivirusInstance.getInstance();
        List<MonitorItem> items = av.getMonitoredDirectories();
        for (MonitorItem item: items) {
            AntivirusInstance.getInstance().removeDirectoryFromMonitor(item.getPath());
        }

        for (Map.Entry<String, Integer> entry: rtpItems.entrySet()) {
            AntivirusInstance.getInstance().addDirectoryToMonitor(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void removeFolderFromMonitor(@NonNull String folder) {
        AntivirusInstance.getInstance().removeDirectoryFromMonitor(folder);
    }

    @Override
    public void addExcludeFolder(@NonNull String folder) {
        AntivirusInstance.getInstance().addExclusionToMonitor(folder);
    }

    @Override
    public void setExcludeFolders(@NonNull Set<String> folders) {
        Antivirus av       = AntivirusInstance.getInstance();
        List<String> items = av.getMonitorExclusions();
        for (String item: items) {
            AntivirusInstance.getInstance().removeExclusionFromMonitor(item);
        }

        for (String folder: folders) {
            AntivirusInstance.getInstance().addExclusionToMonitor(folder);
        }
    }

    @Override
    public void removeExcludeFolder(@NonNull String folder) {
        AntivirusInstance.getInstance().removeExclusionFromMonitor(folder);
    }

    @NonNull
    @Override
    public Map<String, Integer> getFoldersToMonitor() {
        Map<String, Integer> results = new HashMap<>();
        List<MonitorItem> items = AntivirusInstance.getInstance().getMonitoredDirectories();
        for (MonitorItem item: items) {
            results.put(item.getPath(), item.getFlags());
        }
        return results;
    }

    @NonNull
    @Override
    public Set<String> getExcludeFolders() {
        return new HashSet<>(AntivirusInstance.getInstance().getMonitorExclusions());
    }

    @Override
    public void addDefaultDirectories() {
        AntivirusInstance.getInstance().addDefaultDirectories();
    }

    @Override
    public void removeDefaultDirectories() {
        AntivirusInstance.getInstance().removeDefaultDirectories();
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        mAvAction = action;
    }

    @Override
    public void onMonitorEvent(ThreatInfo threatInfo, ThreatType threatType) {
        final String threatPath = threatInfo.getFileFullPath();

        Log.d(TAG, "Monitor event: " + threatInfo.toString());
        if (threatType != null && threatType != ThreatType.None) {
            final Antivirus antivirus = AntivirusInstance.getInstance();
            switch (mAvAction) {
                case DeteteThreat:
                    if (antivirus.removeThreat(threatInfo)) {
                        Log.d(TAG, MONITOR_EVENT_PATH_MSG + threatPath + " was removed");
                    } else {
                        Log.d(TAG, MONITOR_EVENT_PATH_MSG + threatPath + " was not removed");
                    }
                    break;
                case QuarantineThreat:
                    try {
                        antivirus.addToQuarantine(threatInfo);
                        boolean removed = antivirus.removeThreat(threatInfo);
                        if (removed) {
                            Log.d(TAG, MONITOR_EVENT_PATH_MSG + threatPath + " moved to quarantine");
                        } else {
                            Log.d(TAG, MONITOR_EVENT_PATH_MSG + threatPath + " copied to quarantine, couldn't delete");
                        }
                    } catch (QuarantineException e) {
                        Log.d(TAG, MONITOR_EVENT_PATH_MSG + threatPath + " quarantine exception: " + e.getMessage());
                    }
                    break;
                case SkipThreat:
                    Log.d(TAG, MONITOR_EVENT_PATH_MSG + threatPath + " was skipped");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onMonitorEvent(ThreatInfo threatInfo,
                               @SuppressWarnings("deprecation") SuspiciousThreatType threatType) {
        final String threatPath = threatInfo.getFileFullPath();
        final String objectName = threatInfo.getObjectName();

        Log.d(TAG, "Monitor suspicious event: object = " + objectName + ", path = " + threatPath + ", " + Utils.toString(threatInfo.getSeverityLevel()) + " = " + threatInfo.getVirusName());
    }
}
