/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.model.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.monitor.model.settings.MonitorBaseSettings;

import java.util.Set;

public interface Settings extends MonitorBaseSettings {
    @Nullable
    Set<String> getFoldersToMonitor();
    void        setFoldersToMonitor(@NonNull Set<String> items);
}
