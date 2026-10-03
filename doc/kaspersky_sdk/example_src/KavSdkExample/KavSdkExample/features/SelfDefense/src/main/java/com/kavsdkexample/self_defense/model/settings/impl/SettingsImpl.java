/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.self_defense.model.settings.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.kavsdkexample.core.app.features.SettingsHelper;
import com.kavsdkexample.self_defense.model.settings.Settings;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES              = "self_defense_prefs";
    private static final String FOREGROUND_ENABLED_PREF  = "foreground_enabled";

    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isForegroundServiceEnabled() {

        return mPreferences.getBoolean(FOREGROUND_ENABLED_PREF, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
    }

    @Override
    public void setForegroundServiceEnabled(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, FOREGROUND_ENABLED_PREF, value);
    }
}
