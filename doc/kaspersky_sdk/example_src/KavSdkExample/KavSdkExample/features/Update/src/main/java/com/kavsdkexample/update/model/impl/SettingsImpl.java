/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model.impl;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.kavsdkexample.update.model.Settings;
import com.kavsdkexample.core.app.features.SettingsHelper;
import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;

public class SettingsImpl implements Settings {
    private static final String PREFERENCES = "update_prefs";
    private static final String UPDATE_COMPONENT = "updateComponent";
    private static final String UPDATE_UPDATE_SERVER = "updateUpdateServer";
    private static final String UPDATE_SERVER = "updateServer";

    private final SharedPreferences mPreferences;

    public SettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    @NonNull
    public UpdateModelComponentMode getUpdateModelComponent() {
        int mode = mPreferences.getInt(UPDATE_COMPONENT, UpdateModelComponentMode.All.ordinal());
        UpdateModelComponentMode[] values = UpdateModelComponentMode.values();
        if (mode < 0 || mode >= values.length) {
            return UpdateModelComponentMode.All;
        }
        return values[mode];
    }

    @Override
    public void setUpdateModelComponent(@NonNull UpdateModelComponentMode component) {
        SettingsHelper.writeIntPref(mPreferences, UPDATE_COMPONENT, component.ordinal());
    }

    @NonNull
    public UpdateModelUpdateServerMode getUpdateModelUpdateServer() {
        int mode = mPreferences.getInt(UPDATE_UPDATE_SERVER, UpdateModelUpdateServerMode.Random.ordinal());
        UpdateModelUpdateServerMode[] values = UpdateModelUpdateServerMode.values();
        if (mode < 0 || mode >= values.length) {
            return UpdateModelUpdateServerMode.Random;
        }
        return values[mode];
    }

    public void setUpdateModelUpdateServer(@NonNull UpdateModelUpdateServerMode updateServer) {
        SettingsHelper.writeIntPref(mPreferences, UPDATE_UPDATE_SERVER, updateServer.ordinal());
    }

    @Override
    @NonNull
    public String getUpdateServer() {
        String updateServer = mPreferences.getString(UPDATE_SERVER, null);
        return updateServer == null ? "" : updateServer;
    }

    @Override
    public void setUpdateServer(@NonNull String updateServer) {
        SettingsHelper.writeStringPref(mPreferences, UPDATE_SERVER, updateServer);
    }
}