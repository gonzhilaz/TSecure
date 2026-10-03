/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface ScanObserver<T extends ScanResults> {
    @SuppressWarnings("unused")
    void onBasesUnavailable();
    void onError(@NonNull ScanErrorType error);
    void onScanStarted(@NonNull ScanController controller);
    void onScanPaused();
    void onScanResumed();
    void onScanFinished(@NonNull T scanResults);

    enum ScanErrorType {
        PathNotExists,
        PathIsNotFile,
        PathIsNotFolder,
        LicenseError
    }
}
