/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.view.ScannerBaseView;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScanResults;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;

public interface EasyScannerView extends ScannerBaseView<EasyScanResults> {
    void setEasyScannerMode(@NonNull EasyScannerMode mode);
}
