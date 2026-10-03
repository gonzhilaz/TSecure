/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app;

import androidx.annotation.Keep;
import androidx.annotation.UiThread;

@UiThread
@Keep
public interface SdkStatusObserver {
    void onSdkInited();
    void onSdkInitFailed();
}
