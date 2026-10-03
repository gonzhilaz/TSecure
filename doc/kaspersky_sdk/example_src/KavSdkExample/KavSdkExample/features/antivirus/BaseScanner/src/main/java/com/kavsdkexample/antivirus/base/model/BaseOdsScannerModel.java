/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

import androidx.annotation.NonNull;

import java.util.List;

public interface BaseOdsScannerModel<SCANRESULTS extends ScanResults>
                            extends BaseScannerModel<SCANRESULTS> {
    void setObjectsToScan(@NonNull ScanObjectsType objectsToScan, @NonNull String path);
    @NonNull
    ScanObjectsType getObjectsToScanType();
    @NonNull
    String getObjectsToScanPath();
    String getRootUri();
    void setRootUri(@NonNull String rootUri);
    @NonNull
    List<String> getSdCardsOptions();
}
