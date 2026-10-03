/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;

public interface UpdateTaskFactory {
    @NonNull
    Runnable createUpdateTask(@NonNull ThreadManager threadManager,
                              @NonNull UpdateModelObserver observer,
                              @NonNull Settings settings);
}