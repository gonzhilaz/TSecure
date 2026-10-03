/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.model.app.tasks.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.model.app.tasks.ActivateLicenseResultsObserver;
import com.kavsdkexample.model.app.tasks.InitSdkResultsObserver;
import com.kavsdkexample.model.app.tasks.TasksFactory;
import com.kavsdkexample.core.app.model.interactors.ServiceInteractor;

public final class TasksFactoryImpl implements TasksFactory {
    private final Context mContext;

    public TasksFactoryImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    @NonNull
    public Runnable createInitSdkTask(@NonNull ThreadManager          threadManager,
                                      @NonNull ServiceInteractor      serviceInteractor,
                                      @NonNull InitSdkResultsObserver observer) {
        return new InitSdkTask(mContext, threadManager, serviceInteractor, observer);
    }

    @Override
    @NonNull
    public Runnable createActivateLicenseTask(@NonNull ThreadManager                  threadManager,
                                              @NonNull ActivateLicenseResultsObserver observer,
                                              @NonNull String                         activationCode) {
        return new ActivateSdkLicenseTask(threadManager, observer, activationCode);
    }
}
