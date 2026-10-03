/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model.tasks;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;

public interface EulaTasksFactory {
    @NonNull
    Runnable createLoadEulaTask(@NonNull ThreadManager    threadManager,
                                @NonNull EulaLoadObserver observer);
}
