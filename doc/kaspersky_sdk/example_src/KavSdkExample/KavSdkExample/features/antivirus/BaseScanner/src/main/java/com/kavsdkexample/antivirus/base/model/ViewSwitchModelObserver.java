/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ViewSwitchModelObserver {
    void onChangeViewRequest(@NonNull ViewType viewType, @Nullable Object params);

    enum ViewType {
        ScanView,
        ScanResultsView,
        ThreatsInfoView,
        Applications
    }
}
