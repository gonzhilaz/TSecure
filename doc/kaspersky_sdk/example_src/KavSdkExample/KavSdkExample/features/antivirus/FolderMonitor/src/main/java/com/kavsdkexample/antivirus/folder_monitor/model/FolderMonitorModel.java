/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.model;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseModel;

import java.util.Set;

public interface FolderMonitorModel extends MonitorBaseModel {
    void        addFolderForMonitoring(@NonNull String folder);
    void        removeFolderFromMonitoring(@NonNull String folder);
    void        removeAllFolders();

    @NonNull
    Set<String> getMonitoredFolders();
}
