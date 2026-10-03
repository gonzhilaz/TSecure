/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.model.settings.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.kavsdkexample.antivirus.base.monitor.model.settings.impl.DefaultMonitorSettings;
import com.kavsdkexample.antivirus.rtp_monitor.model.settings.Settings;
import com.kavsdkexample.core.app.features.SettingsHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class SettingsImpl extends DefaultMonitorSettings implements Settings {
    private static final String TAG                = SettingsImpl.class.getSimpleName();

    private static final String PREFERENCES        = "rtp_monitor_prefs";
    private static final String FOLDERS_TO_MONITOR = "foldersToMonitor";
    private static final String FOLDERS_TO_EXCLUDE = "foldersToExclude";

    public SettingsImpl(final Context context) {
        super(context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE));
    }

    @Override
    @Nullable
    public Map<String, Integer> getFoldersToMonitor() {
        JSONObject object;
        Map<String, Integer> results = new HashMap<>();
        String jsonString = getPreferences().getString(FOLDERS_TO_MONITOR, null);
        if (jsonString == null) {
            return null;
        }

        try {
            object = new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to read value JSON for preference" + FOLDERS_TO_MONITOR, e);
            return results;
        }
        Iterator<String> keys = object.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            try {
                results.put(key, (Integer) object.get(key));
            } catch (Exception e) {
                Log.e(TAG, "Failed to read value from JSON for key:" + key, e);
            }
        }
        return results;
    }

    @Override
    public void setFoldersToMonitor(@NonNull Map<String, Integer> rtpItems) {
        JSONObject object = new JSONObject();
        for (Map.Entry<String, Integer> item: rtpItems.entrySet()) {
            try {
                object.put(item.getKey(), item.getValue());
            } catch (JSONException e) {
                throw new RuntimeException("Failed convert to json: ", e);
            }
        }
        SettingsHelper.writeStringPref(getPreferences(), FOLDERS_TO_MONITOR, object.toString());
    }

    @Override
    @Nullable
    public Set<String> getFoldersToExclude() {
        return SettingsHelper.getStringSet(getPreferences(), FOLDERS_TO_EXCLUDE, null);
    }

    @Override
    public void setFoldersToExclude(@NonNull Set<String> folders) {
        SettingsHelper.putStringSet(getPreferences(), FOLDERS_TO_EXCLUDE, folders);
    }
}
