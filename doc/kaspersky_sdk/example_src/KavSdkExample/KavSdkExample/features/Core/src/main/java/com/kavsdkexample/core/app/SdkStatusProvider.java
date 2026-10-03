/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface SdkStatusProvider {
    void subscribe(@NonNull SdkStatusObserver observer);
}
