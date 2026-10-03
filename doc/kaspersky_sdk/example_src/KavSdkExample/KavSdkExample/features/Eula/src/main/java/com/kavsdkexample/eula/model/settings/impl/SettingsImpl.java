/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model.settings.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.kavsdkexample.core.app.features.SettingsHelper;
import com.kavsdkexample.eula.model.settings.Settings;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES         = "eula_prefs";
    private static final String EULA_ACCEPTED_PREF  = "eulaAccepted";

    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isEulaAccepted() {
        return mPreferences.getBoolean(EULA_ACCEPTED_PREF, false);
    }

    @Override
    public void setEulaAccepted(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, EULA_ACCEPTED_PREF, value);
    }
}
