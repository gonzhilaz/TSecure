/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.monitor.view.MonitorBaseView;

import java.util.Set;

public interface FolderMonitorView extends MonitorBaseView {
    void setTryCure(boolean enabled);
    void setFoldersToMonitor(@NonNull Set<String> folders);
    void showAddFolderFragment();
    void showDeleteFolderFragment(@NonNull String path);
    void showClearAllFragment();
    void showAddedFolder(@NonNull String folder);
    void showRemovedFolder(@NonNull String folder);
}
