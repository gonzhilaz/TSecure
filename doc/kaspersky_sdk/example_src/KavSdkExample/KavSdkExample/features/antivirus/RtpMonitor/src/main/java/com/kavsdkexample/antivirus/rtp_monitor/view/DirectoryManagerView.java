/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.view.BaseView;

import java.util.Map;
import java.util.Set;

public interface DirectoryManagerView extends BaseView {
    void updateFoldersToMonitor(@NonNull Map<String, Integer> items);
    void updateExcludedFolders(@NonNull Set<String> items);

    void addMonitoringFolder(@NonNull String folder);
    void removeMonitoringFolder(@NonNull String folder);
    void addExclusionFolder(@NonNull String folder);
    void removeExclusionFolder(@NonNull String folder);
}
