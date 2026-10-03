/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.database;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.model.base.BaseResultsObserver;

public interface RequestDbResultsObserver extends BaseResultsObserver {
    @UiThread
    void onRequestCompleted(@NonNull String result);
}
