/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model.tasks.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.eula.model.tasks.EulaLoadObserver;
import com.kavsdkexample.eula.model.tasks.EulaTasksFactory;

public class EulaTasksFactoryImpl implements EulaTasksFactory {
    private final Context mContext;

    public EulaTasksFactoryImpl(@NonNull Context context) {
        mContext = context;
    }
    @NonNull
    @Override
    public Runnable createLoadEulaTask(@NonNull ThreadManager    threadManager,
                                       @NonNull EulaLoadObserver observer) {
        return new LoadEulaTask(mContext, threadManager, observer);
    }
}
