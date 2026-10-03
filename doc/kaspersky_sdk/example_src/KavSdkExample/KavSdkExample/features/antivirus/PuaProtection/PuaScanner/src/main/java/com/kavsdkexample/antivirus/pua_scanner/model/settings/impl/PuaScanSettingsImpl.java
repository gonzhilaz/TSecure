/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_scanner.model.settings.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.pua_scanner.model.settings.PuaScanSettings;

public class PuaScanSettingsImpl implements PuaScanSettings {
    @Override
    public void setAvAction(@NonNull AvAction action) {

    }

    @Override
    public void setCloudOnlyScan(boolean enabled) {

    }

    @Override
    public void setAllowCloudScan(boolean enabled) {

    }

    @Override
    public void setScanRiskware(boolean enabled) {

    }

    @Override
    public void setScanSuspicious(boolean enabled) {

    }

    @Override
    public void setTryCure(boolean enabled) {

    }

    @NonNull
    @Override
    public AvAction getAvAction() {
        return null;
    }

    @Override
    public boolean getCloudOnlyScan() {
        return false;
    }

    @Override
    public boolean getAllowCloudScan() {
        return false;
    }

    @Override
    public boolean getScanRiskware() {
        return false;
    }

    @Override
    public boolean getScanSuspicious() {
        return false;
    }

    @Override
    public boolean getTryCure() {
        return false;
    }
}
