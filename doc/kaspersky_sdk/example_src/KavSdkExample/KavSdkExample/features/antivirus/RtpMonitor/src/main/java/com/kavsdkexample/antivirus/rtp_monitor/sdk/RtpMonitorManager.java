/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.sdk;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseManager;

import java.util.Map;
import java.util.Set;

public interface RtpMonitorManager extends MonitorBaseManager {
    void                 addFolderToMonitor(@NonNull String folder, int flags);
    void                 setFoldersToMonitor(@NonNull Map<String, Integer> rtpItems);
    void                 removeFolderFromMonitor(@NonNull String folder);
    @NonNull
    Map<String, Integer> getFoldersToMonitor();

    void                 addExcludeFolder(@NonNull String folder);
    void                 setExcludeFolders(@NonNull Set<String> folders);
    void                 removeExcludeFolder(@NonNull String folder);
    @NonNull
    Set<String>          getExcludeFolders();

    void addDefaultDirectories();
    void removeDefaultDirectories();

    void setAvAction(@NonNull AvAction action);
}
