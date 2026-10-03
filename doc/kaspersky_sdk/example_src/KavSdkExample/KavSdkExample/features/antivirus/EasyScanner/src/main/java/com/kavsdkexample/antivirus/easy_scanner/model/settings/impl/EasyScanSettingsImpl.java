/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.model.settings.impl;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;
import com.kavsdkexample.antivirus.easy_scanner.model.settings.EasyScanSettings;
import com.kavsdkexample.core.app.features.SettingsHelper;

public class EasyScanSettingsImpl implements EasyScanSettings {
    private static final String PREFERENCES         = "easy_scanner_prefs";
    private static final String EASY_SCANNER_MODE   = "easyScannerMode";
    private static final String EASY_AV_ACTION      = "easyAvAction";


    private final SharedPreferences mPreferences;

    public EasyScanSettingsImpl(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    @NonNull
    public EasyScannerMode getEasyScannerMode() {
        int mode = mPreferences.getInt(EASY_SCANNER_MODE, EasyScannerMode.Basic.ordinal());
        EasyScannerMode[] values = EasyScannerMode.values();

        if (mode < 0 || mode >= values.length) {
            return EasyScannerMode.Basic;
        }
        return values[mode];
    }

    @Override
    public void setEasyScannerMode(@NonNull EasyScannerMode mode) {
        SettingsHelper.writeIntPref(mPreferences, EASY_SCANNER_MODE, mode.ordinal());
    }

    @NonNull
    @Override
    public AvAction getAvAction() {
        int action = mPreferences.getInt(EASY_AV_ACTION, AvAction.SkipThreat.ordinal());
        AvAction[] values = AvAction.values();
        if (action < 0 || action >= values.length) {
            return AvAction.SkipThreat;
        }
        return values[action];

    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        SettingsHelper.writeIntPref(mPreferences, EASY_AV_ACTION, action.ordinal());
    }

    @Override
    public void setCloudOnlyScan(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAllowCloudScan(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScanRiskware(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setScanSuspicious(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTryCure(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getCloudOnlyScan() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getAllowCloudScan() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getScanRiskware() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getScanSuspicious() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getTryCure() {
        throw new UnsupportedOperationException();
    }
}
