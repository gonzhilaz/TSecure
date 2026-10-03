/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.repository.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.self_checker.model.SelfCheckResultsObserver;
import com.kavsdkexample.antivirus.self_checker.model.tasks.TasksFactory;
import com.kavsdkexample.core.app.utils.ThreadManager;


public final class TasksFactoryImpl extends    com.kavsdkexample.antivirus.base.sdk.impl.TasksFactoryImpl
                                    implements TasksFactory {
    public TasksFactoryImpl(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public Runnable createSelfCheckTask(@NonNull ThreadManager threadManager,
                                        @NonNull SelfCheckResultsObserver observer) {
        return new SelfCheckTask(threadManager, observer);
    }

}
