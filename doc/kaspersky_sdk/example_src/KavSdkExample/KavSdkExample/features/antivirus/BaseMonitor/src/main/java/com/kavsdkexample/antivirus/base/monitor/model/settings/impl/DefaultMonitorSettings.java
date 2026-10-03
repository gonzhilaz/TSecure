/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.model.settings.impl;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.settings.impl.DefaultAvSettings;
import com.kavsdkexample.antivirus.base.monitor.model.settings.MonitorBaseSettings;
import com.kavsdkexample.core.app.features.SettingsHelper;

public class DefaultMonitorSettings extends DefaultAvSettings implements MonitorBaseSettings {
    private static final String MONITOR_STATE_PREF     = "monitorEnabled";
    private static final String MAX_FILE_SIZE_PREF     = "monitorMaxFileSize";

    protected DefaultMonitorSettings(@NonNull SharedPreferences preferences) {
        super(preferences);
    }

    @Override
    public void setMonitorState(boolean enabled) {
        SettingsHelper.writeBooleanPref(getPreferences(), MONITOR_STATE_PREF, enabled);
    }

    @Override
    public boolean getMonitorState() {
        return getPreferences().getBoolean(MONITOR_STATE_PREF, false);
    }

    @Override
    public void setMaxFileCheckSize(long size) {
        SettingsHelper.writeLongPref(getPreferences(), MAX_FILE_SIZE_PREF, size);
    }

    @Override
    public long getMaxFileCheckSize() {
        return getPreferences().getLong(MAX_FILE_SIZE_PREF, 0L);
    }
}
