/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.self_checker.model.tasks;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.self_checker.model.SelfCheckResultsObserver;
import com.kavsdkexample.core.app.utils.ThreadManager;

public interface TasksFactory extends com.kavsdkexample.antivirus.base.model.tasks.TasksFactory {
    @NonNull
    Runnable createSelfCheckTask(@NonNull ThreadManager threadManager,
                                 @NonNull SelfCheckResultsObserver observer);
}
