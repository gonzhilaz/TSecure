/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.core.app.view.BaseView;

public interface ScanResultsView<SCANRESULTS extends ScanResults> extends BaseView {
    void    showScanResults(@NonNull SCANRESULTS scanResults);
    boolean isScanResultsShown();
}
