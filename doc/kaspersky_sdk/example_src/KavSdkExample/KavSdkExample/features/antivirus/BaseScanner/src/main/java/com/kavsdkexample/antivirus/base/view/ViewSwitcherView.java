/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.repository.storage.ThreatType;
import com.kavsdkexample.core.app.view.BaseView;

public interface ViewSwitcherView extends BaseView {
    void showScanView();
    void showScanResultsView();
    void showThreatsInfoView(@NonNull ThreatType threatType);
    void showApplications(boolean odsApplications);
}