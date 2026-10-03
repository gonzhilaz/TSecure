/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.repository.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.root_checker.model.RootCheckResultsObserver;
import com.kavsdkexample.antivirus.root_checker.model.tasks.TasksFactory;
import com.kavsdkexample.core.app.utils.ThreadManager;

public final class TasksFactoryImpl extends    com.kavsdkexample.antivirus.base.sdk.impl.TasksFactoryImpl
                                    implements TasksFactory {
    public TasksFactoryImpl(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public Runnable createRootCheckTask(@NonNull ThreadManager threadManager,
                                        @NonNull RootCheckResultsObserver observer) {
        return new RootCheckTask(threadManager, observer);
    }

}
