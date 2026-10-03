/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antiphishing.repository.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.antiphishing.model.WebFilterInitObserver;
import com.kavsdkexample.antiphishing.model.tasks.TasksFactory;
import com.kavsdkexample.antiphishing.repository.sdk.SdkLocalStatusObserver;
import com.kavsdkexample.antiphishing.model.WebFilterModel;
import com.kavsdkexample.core.app.utils.ThreadManager;

public class TasksFactoryImpl implements TasksFactory {
    private final Context              mContext;
    private final WebFilterManagerImpl mWebFilterManager;

    public TasksFactoryImpl(@NonNull Context context, @NonNull WebFilterManagerImpl  webFilterManager) {
        mContext          = context;
        mWebFilterManager = webFilterManager;
    }

    @Override
    @NonNull
    public Runnable createWebFilterManagerInitTask(@NonNull ThreadManager         threadManager,
                                                   @NonNull WebFilterInitObserver webFilterInitObserver) {
        return new WebFilterManagerInitTask(threadManager, mWebFilterManager, webFilterInitObserver);
    }

    @Override
    @NonNull
    public SdkLocalStatusObserver createSdkLocalStatusObserver(@NonNull WebFilterModel webFilterModel, @NonNull ThreadManager threadManager) {
        return new SdkLocalStatusObserverImpl(webFilterModel, threadManager);
    }
}
