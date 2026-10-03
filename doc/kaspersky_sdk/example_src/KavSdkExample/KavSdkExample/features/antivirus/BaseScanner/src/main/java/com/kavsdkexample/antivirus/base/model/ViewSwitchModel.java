/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.core.app.model.BaseModel;

public interface ViewSwitchModel extends BaseModel {
    boolean needDisplayScanResults();
    void    resetDisplayScanResults();
    void    switchToScanView();
    void    switchToScanResultsView();
    void    switchToThreatsInfoView(@NonNull ThreatType threatType);
    void    switchToOdsApplicationThreats();
}
