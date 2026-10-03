/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.model;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.antivirus.base.model.BaseScannerModel;

@UiThread
public interface EasyScannerModel extends BaseScannerModel<EasyScanResults> {
    @NonNull
    EasyScannerMode getEasyScannerMode();
    void setEasyScannerMode(@NonNull EasyScannerMode mode);
}
