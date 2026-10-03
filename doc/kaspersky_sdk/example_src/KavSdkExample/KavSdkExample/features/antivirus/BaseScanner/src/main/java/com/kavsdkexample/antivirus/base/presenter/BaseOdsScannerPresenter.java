/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObjectsType;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.view.ScannerBaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface BaseOdsScannerPresenter<VIEW        extends ScannerBaseView<SCANRESULTS>,
                                         VIEWSTATE   extends BaseViewState,
                                         SCANRESULTS extends ScanResults>
                               extends BaseScannerPresenter<VIEW, VIEWSTATE> {
    void   setObjectsToScan(@NonNull ScanObjectsType objectsToScan, @NonNull String path, boolean radioCheckChanged);
    void   setCloudOnlyScan(boolean enabled);
    void   setAllowCloudScan(boolean enabled);
    void   setScanRiskware(boolean enabled);
    void   setScanSuspicious(boolean enabled);
    @NonNull
    String getCurrentScanPath();
}
