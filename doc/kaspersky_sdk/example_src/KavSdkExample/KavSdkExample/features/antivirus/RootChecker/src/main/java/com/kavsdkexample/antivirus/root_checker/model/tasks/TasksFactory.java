/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.root_checker.model.tasks;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.root_checker.model.RootCheckResultsObserver;
import com.kavsdkexample.core.app.utils.ThreadManager;

public interface TasksFactory extends com.kavsdkexample.antivirus.base.model.tasks.TasksFactory {
    @NonNull
    Runnable createRootCheckTask(@NonNull ThreadManager            threadManager,
                                 @NonNull RootCheckResultsObserver observer);
}
