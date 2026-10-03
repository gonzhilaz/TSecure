/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import androidx.annotation.NonNull;

public interface ThreadManager {
    void runOnUiThread(@NonNull Runnable runnable);
    void checkUiThread();
    void checkWorkerThread();
}
