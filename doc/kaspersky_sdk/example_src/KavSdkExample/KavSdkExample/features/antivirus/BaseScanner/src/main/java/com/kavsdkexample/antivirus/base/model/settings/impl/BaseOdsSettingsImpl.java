/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model.settings.impl;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObjectsType;
import com.kavsdkexample.antivirus.base.model.settings.BaseOdsSettings;
import com.kavsdkexample.core.app.features.SettingsHelper;

public class BaseOdsSettingsImpl extends DefaultAvSettings implements BaseOdsSettings {
    private static final String OBJECTS_TO_SCAN_TYPE_PREF = "objectToScanType";
    private static final String OBJECTS_TO_SCAN_PATH_PREF = "objectToScanPath";

    private static final String ROOT_URI = "rootUri";

    protected BaseOdsSettingsImpl(@NonNull SharedPreferences preferences) {
        super(preferences);
    }

    @Override
    public void setObjectsToScan(@NonNull ScanObjectsType objectsToScan, @NonNull String path) {
        SharedPreferences prefs = getPreferences();
        SettingsHelper.writeIntPref(prefs, OBJECTS_TO_SCAN_TYPE_PREF, objectsToScan.ordinal());
        SettingsHelper.writeStringPref(prefs, OBJECTS_TO_SCAN_PATH_PREF, path);
    }

    @NonNull
    @Override
    public ScanObjectsType getObjectsToScanType() {
        int action = getPreferences().getInt(OBJECTS_TO_SCAN_TYPE_PREF, ScanObjectsType.AllFiles.ordinal());
        ScanObjectsType[] values = ScanObjectsType.values();
        if (action < 0 || action >= values.length) {
            return ScanObjectsType.AllFiles;
        }
        return values[action];
    }

    @NonNull
    @Override
    public String getObjectsToScanPath() {
        return getPreferences().getString(OBJECTS_TO_SCAN_PATH_PREF, "");
    }

    @Override
    public String getRootUri() {
        return getPreferences().getString(ROOT_URI, null);
    }

    @Override
    public void setRootUri(@NonNull String rootUri) {
        SharedPreferences prefs = getPreferences();
        SettingsHelper.writeStringPref(prefs, ROOT_URI, rootUri);
    }
}
