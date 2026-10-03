/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.model.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.kavsdkexample.fingerprintmonitor.model.Settings;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES     = "fingerprint_monitor_prefs";
    private static final String MONITOR_ENABLED = "monitor_enabled";

    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void saveMonitorEnabled(boolean enabled) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(MONITOR_ENABLED, enabled);
        editor.apply();
    }

    @Override
    public boolean getMonitorEnabled() {
        return mPreferences.getBoolean(MONITOR_ENABLED, false);
    }

}
