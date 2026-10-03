/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_connectivity.repository.settings.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.kavsdkexample.core.app.features.SettingsHelper;
import com.kavsdkexample.secure_connectivity.repository.settings.Settings;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES                    = "secure_connectivity_prefs";
    private static final String LOAD_ONLY_TRUSTED_URLS_ENABLED = "loadOnlyTrustedUrlsEnabled";

    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void setLoadOnlyTrustedUrlsEnabled(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, LOAD_ONLY_TRUSTED_URLS_ENABLED, value);
    }

    @Override
    public boolean isLoadOnlyTrustedUrlsEnabled() {
        return mPreferences.getBoolean(LOAD_ONLY_TRUSTED_URLS_ENABLED, false);
    }
}
