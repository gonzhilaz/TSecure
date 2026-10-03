/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.model.settings.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.monitor.model.settings.impl.DefaultMonitorSettings;
import com.kavsdkexample.antivirus.folder_monitor.model.settings.Settings;
import com.kavsdkexample.core.app.features.SettingsHelper;

import java.util.Set;

public class SettingsImpl extends DefaultMonitorSettings implements Settings {
    private static final String PREFERENCES         = "folder_monitor_prefs";
    private static final String FOLDERS_TO_MONITOR  = "foldersToMonitor";

    public SettingsImpl(final Context context) {
        super(context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
    }

    @Override
    @Nullable
    public Set<String> getFoldersToMonitor() {
        return SettingsHelper.getStringSet(getPreferences(), FOLDERS_TO_MONITOR, null);
    }

    @Override
    public void setFoldersToMonitor(@NonNull Set<String> folders) {
        SettingsHelper.putStringSet(getPreferences(), FOLDERS_TO_MONITOR, folders);
    }
}
