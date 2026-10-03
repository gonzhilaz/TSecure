/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface InitSdkResultsObserver {
    void onSdkInited(long initTimeMs);
    void onInitException(@NonNull Exception e);
}
