/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.settings.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.kavsdkexample.core.app.features.SettingsHelper;
import com.kavsdkexample.model.app.settings.Settings;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES              = "main_prefs";
    private static final String WORK_WITHOUT_PERMISSIONS = "permissionsSkipped";
    private static final String WIZARD_COMPLETED         = "wizardCompleted";

    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public boolean getWorkWithoutPermissions() {
        return mPreferences.getBoolean(WORK_WITHOUT_PERMISSIONS, false);
    }

    @Override
    public void setWorkWithoutPermissions(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, WORK_WITHOUT_PERMISSIONS, value);
    }

    @Override
    public void setWizardCompleted(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, WIZARD_COMPLETED, value);
    }

    @Override
    public boolean getWizardCompleted() {
        return mPreferences.getBoolean(WIZARD_COMPLETED, false);
    }
}
