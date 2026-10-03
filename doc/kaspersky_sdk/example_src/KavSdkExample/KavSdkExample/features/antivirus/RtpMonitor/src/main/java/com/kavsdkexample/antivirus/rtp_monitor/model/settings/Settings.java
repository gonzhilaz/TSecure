/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.model.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.monitor.model.settings.MonitorBaseSettings;

import java.util.Map;
import java.util.Set;

public interface Settings extends MonitorBaseSettings {
    @Nullable
    Map<String, Integer> getFoldersToMonitor();
    void setFoldersToMonitor(@NonNull Map<String, Integer> items);

    @Nullable
    Set<String> getFoldersToExclude();
    void setFoldersToExclude(@NonNull Set<String> folders);
}
