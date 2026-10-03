/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

import androidx.annotation.NonNull;

public interface BaseScannerModelObserver<SCANRESULTS extends ScanResults> {
    @SuppressWarnings("unused")
    void onBasesUnavailable();
    void onError(@NonNull ScanObserver.ScanErrorType error);
    void onScanPaused();
    void onScanResumed();
    void onScanFinished(@NonNull SCANRESULTS scanResults);
}
