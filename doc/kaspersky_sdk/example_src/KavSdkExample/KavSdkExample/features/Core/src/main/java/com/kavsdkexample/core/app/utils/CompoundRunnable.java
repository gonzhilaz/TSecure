/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.utils;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public final class CompoundRunnable implements Runnable {
    private final List<Runnable> mRunnables;

    public CompoundRunnable(@NonNull Runnable... runnables) {
       mRunnables = Arrays.asList(runnables);
    }

    @Override
    public void run() {
        for (Runnable runnable : mRunnables) {
            runnable.run();
        }
    }
}
