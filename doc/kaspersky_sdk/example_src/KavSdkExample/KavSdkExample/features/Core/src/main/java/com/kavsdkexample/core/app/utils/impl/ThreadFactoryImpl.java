/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils.impl;

import android.os.Looper;
import androidx.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class ThreadFactoryImpl implements ThreadFactory {
    private static final String DEFAULT_THREAD_NAME = "AppThreadPool-";

    private final AtomicLong mCount = new AtomicLong(0);

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(DEFAULT_THREAD_NAME + mCount.getAndIncrement());
        thread.setUncaughtExceptionHandler(Looper.getMainLooper().getThread().getUncaughtExceptionHandler());
        return thread;
    }
 }
