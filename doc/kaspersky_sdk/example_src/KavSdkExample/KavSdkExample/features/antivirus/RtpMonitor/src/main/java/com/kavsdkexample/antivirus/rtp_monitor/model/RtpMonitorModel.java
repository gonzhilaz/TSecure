/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.model;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseModel;

import java.util.Map;
import java.util.Set;

public interface RtpMonitorModel extends MonitorBaseModel {
    void                 addFolderForMonitoring(@NonNull String folder, int flags);
    void                 removeFolderFromMonitoring(@NonNull String folder);
    void                 addExcludedFolder(@NonNull String folder);
    void                 removeExcludedFolder(@NonNull String folder);

    void                 addDefaultDirectories();
    void                 removeDefaultDirectories();

    @NonNull
    Map<String, Integer> getMonitoredFolders();
    @NonNull
    Set<String>          getExcludedFolders();
}
