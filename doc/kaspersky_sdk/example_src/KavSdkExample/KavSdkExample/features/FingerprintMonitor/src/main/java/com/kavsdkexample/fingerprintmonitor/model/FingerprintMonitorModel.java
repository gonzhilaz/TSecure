/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.fingerprintmonitor.model;

import android.content.Context;

import com.kavsdkexample.core.ui.UiProvider;
import com.kavsdkexample.core.app.model.BaseModel;

public interface FingerprintMonitorModel extends BaseModel {
    void enable();

    void disable();

    boolean isEnabled();

    Context getContext();

    UiProvider getUiProvider();
}
