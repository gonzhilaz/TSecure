/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils.impl;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;

public final class ThreadManagerImpl implements ThreadManager {
    private final Handler mHandler;

    public ThreadManagerImpl() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void runOnUiThread(@NonNull Runnable runnable) {
        mHandler.post(runnable);
    }

    @Override
    public void checkUiThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Execution in main thread is expected here");
        }
    }

    @Override
    public void checkWorkerThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Execution in worker thread is expected here");
        }
    }
}
