/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.sdk;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseManager;

import java.util.Set;

public interface FolderMonitorManager extends MonitorBaseManager {
    void        addFolderToMonitor(@NonNull String folder);
    void        setFoldersToMonitor(@NonNull Set<String> folders);
    void        removeFolderFromMonitor(@NonNull String folder);
    void        removeAllFolders();
    @NonNull
    Set<String> getFoldersToMonitor();

    void        applyCureInfected(final boolean value);
}
