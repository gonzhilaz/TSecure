/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.easy_scanner.model.settings;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.settings.AvFeatureSettings;
import com.kavsdkexample.antivirus.easy_scanner.model.EasyScannerMode;

public interface EasyScanSettings extends AvFeatureSettings {
    @NonNull
    EasyScannerMode getEasyScannerMode();
    void setEasyScannerMode(@NonNull EasyScannerMode mode);
}
