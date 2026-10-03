/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.model.settings.impl;

import android.content.Context;

import com.kavsdkexample.antivirus.pua_monitor.model.settings.Settings;
import com.kavsdkexample.antivirus.base.monitor.model.settings.impl.DefaultMonitorSettings;
import com.kavsdkexample.core.app.features.SettingsHelper;

public class SettingsImpl extends DefaultMonitorSettings implements Settings {
    private static final String PREFERENCES           = "pua_monitor_prefs";
    private static final String MISSED_APP_CHECK_PREF = "missedAppCheck";

    public SettingsImpl(final Context context) {
        super(context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
    }

    @Override
    public void setMissedAppCheckState(boolean value) {
        SettingsHelper.writeBooleanPref(getPreferences(), MISSED_APP_CHECK_PREF, value);
    }

    @Override
    public boolean getMissedAppCheckState() {
        return getPreferences().getBoolean(MISSED_APP_CHECK_PREF, false);
    }
}
