/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model.settings;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.ScanObjectsType;

public interface BaseOdsSettings extends AvFeatureSettings {
    void setObjectsToScan(@NonNull ScanObjectsType objectsToScan, @NonNull String path);
    @NonNull ScanObjectsType getObjectsToScanType();
    @NonNull String          getObjectsToScanPath();
    String getRootUri();
    void setRootUri(@NonNull String rootUri);
}
