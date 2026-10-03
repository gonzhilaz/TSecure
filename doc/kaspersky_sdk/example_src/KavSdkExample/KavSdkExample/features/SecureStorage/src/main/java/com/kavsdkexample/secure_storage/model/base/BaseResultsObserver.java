/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.base;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

public interface BaseResultsObserver {
    @UiThread
    void onError(@NonNull String message);
}
