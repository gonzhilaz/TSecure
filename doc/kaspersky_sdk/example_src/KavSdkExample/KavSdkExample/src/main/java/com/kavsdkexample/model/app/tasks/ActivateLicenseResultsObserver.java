/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

@UiThread
public interface ActivateLicenseResultsObserver {
    void onSuccess(long expirationDate);
    void onFailed(@NonNull Exception exception);
}
