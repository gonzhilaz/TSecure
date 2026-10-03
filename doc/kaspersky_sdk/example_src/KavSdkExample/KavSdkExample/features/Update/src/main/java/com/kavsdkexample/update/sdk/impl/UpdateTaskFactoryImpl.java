/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.sdk.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.update.model.Settings;
import com.kavsdkexample.update.model.UpdateTaskFactory;
import com.kavsdkexample.update.model.UpdateModelObserver;

public final class UpdateTaskFactoryImpl implements UpdateTaskFactory {

    public UpdateTaskFactoryImpl() {
    }

    @Override
    @NonNull
    public Runnable createUpdateTask(@NonNull ThreadManager threadManager,
                                     @NonNull UpdateModelObserver observer,
                                     @NonNull Settings settings) {
        return new UpdateTask(threadManager, observer, settings);
    }
}
