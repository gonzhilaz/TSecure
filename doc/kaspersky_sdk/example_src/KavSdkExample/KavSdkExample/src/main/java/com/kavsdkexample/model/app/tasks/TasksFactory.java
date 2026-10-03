/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.tasks;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;
import com.kavsdkexample.core.app.utils.ThreadManager;

public interface TasksFactory {
    @NonNull
    Runnable createInitSdkTask(@NonNull ThreadManager          threadManager,
                               @NonNull ServiceInteractor      serviceInteractor,
                               @NonNull InitSdkResultsObserver observer);
    @NonNull
    Runnable createActivateLicenseTask(@NonNull ThreadManager                  threadManager,
                                       @NonNull ActivateLicenseResultsObserver observer,
                                       @NonNull String                         activationCode);
}
