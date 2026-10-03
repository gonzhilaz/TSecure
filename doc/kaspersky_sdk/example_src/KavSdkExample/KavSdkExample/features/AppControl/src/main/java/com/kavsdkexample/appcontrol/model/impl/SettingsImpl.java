/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.appcontrol.model.impl;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.kavsdkexample.appcontrol.model.Settings;
import com.kavsdkexample.core.app.features.SettingsHelper;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES = "appcontrol_prefs";
    private static final String APPCONTOL_BUTTON = "appcontrol_button";
    private static final String WINDOWS_MANAGER_FOR_BLOCKING_BUTTON = "windows_manager_for_blocking_button";

    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void saveAppcontrolButtonStatus(boolean status) {
        SettingsHelper.writeBooleanPref(mPreferences, APPCONTOL_BUTTON, false);
    }

    @Override
    public void saveWindowManagerForBlockingButton(boolean status) {
        SettingsHelper.writeBooleanPref(mPreferences, WINDOWS_MANAGER_FOR_BLOCKING_BUTTON, false);
    }

    @Override
    public boolean getAppcontrolButtonStatus() {
        return mPreferences.getBoolean(APPCONTOL_BUTTON, false);
    }

    @Override
    public boolean getWindowManagerForBlockingButton() {
        return mPreferences.getBoolean(WINDOWS_MANAGER_FOR_BLOCKING_BUTTON, false);
    }
}