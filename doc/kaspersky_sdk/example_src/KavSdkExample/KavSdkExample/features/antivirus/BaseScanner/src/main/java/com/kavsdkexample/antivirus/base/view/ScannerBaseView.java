/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObserver;
import com.kavsdkexample.antivirus.base.model.ScanResults;
import com.kavsdkexample.antivirus.base.model.AvAction;

public interface ScannerBaseView<SCANRESULTS extends ScanResults> extends AntivirusBaseView {
    void setAvAction(@NonNull AvAction action);
    void showScanResults(@NonNull SCANRESULTS scanResults);
    void setScanButtonState(@NonNull ScanButtonState state);
    void setPauseButtonState(@NonNull PauseButtonState state);
    void showError(@NonNull ScanObserver.ScanErrorType error);

    enum ScanButtonState {
        Start,
        Stop,
        Stopping
    }

    enum PauseButtonState {
        Disabled,
        Pause,
        Resume
    }
}
