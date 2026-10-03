/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.settings.impl;

import android.content.Context;
import android.content.SharedPreferences;

import com.kavsdkexample.antiphishing.repository.settings.Settings;
import com.kavsdkexample.core.app.features.SettingsHelper;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES                   = "antiphishing_prefs";
    private static final String WEB_FILTER_ENABLED            = "webfilterEnabled";
    private static final String WEB_FILTER_WIFI_PROXY_ENABLED = "wifiProxyEnabled";
    private static final String WEB_FILTER_PROXY_PORT         = "proxyPort";
    private static final String EXT_CATEGORIES_ENABLED        = "extCategoriesEnabled";
    private static final String IGNORE_POWER_SAVE_MODE_ENABLED = "ignorePowerSaveModeEnabled";
    private static final int    PORT_NUMBER                   = 3128;
    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void saveExtCategoriesState(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, EXT_CATEGORIES_ENABLED, value);
    }

    @Override
    public boolean getExtCategoriesState() {
        return mPreferences.getBoolean(EXT_CATEGORIES_ENABLED, false);
    }

    @Override
    public void saveIgnorePowerSaveModeState(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, IGNORE_POWER_SAVE_MODE_ENABLED, value);
    }

    @Override
    public boolean getIgnorePowerSaveModeState() {
        return mPreferences.getBoolean(IGNORE_POWER_SAVE_MODE_ENABLED, false);
    }

    @Override
    public boolean getWebFilterState() {
        return mPreferences.getBoolean(WEB_FILTER_ENABLED, false);
    }

    @Override
    public boolean getWifiProxyState() {
        return mPreferences.getBoolean(WEB_FILTER_WIFI_PROXY_ENABLED, false);
    }

    @Override
    public int getProxyPort() {
        return mPreferences.getInt(WEB_FILTER_PROXY_PORT, PORT_NUMBER);
    }

    @Override
    public void saveWebFilterState(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, WEB_FILTER_ENABLED, value);
    }

    @Override
    public void saveWifiProxyState(boolean value) {
        SettingsHelper.writeBooleanPref(mPreferences, WEB_FILTER_WIFI_PROXY_ENABLED, value);
    }

    @Override
    public void saveProxyPort(int port) {
        SettingsHelper.writeIntPref(mPreferences, WEB_FILTER_PROXY_PORT, PORT_NUMBER);
    }
}
