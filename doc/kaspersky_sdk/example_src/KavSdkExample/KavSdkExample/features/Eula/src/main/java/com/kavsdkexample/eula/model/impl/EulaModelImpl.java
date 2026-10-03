/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.eula.model.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.features.SdkFeature;
import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.eula.model.EulaManageableModel;
import com.kavsdkexample.eula.model.EulaModelStatusObserver;
import com.kavsdkexample.eula.model.settings.Settings;
import com.kavsdkexample.eula.model.tasks.EulaLoadObserver;
import com.kavsdkexample.eula.model.tasks.EulaTasksFactory;

import java.util.concurrent.ExecutorService;

@UiThread
public class EulaModelImpl extends     BaseModelImpl
                           implements  EulaManageableModel,
                                       EulaLoadObserver {
    private final ThreadManager              mThreadManager;
    private final ExecutorService            mExecutorService;
    private final EulaTasksFactory           mTasksFactory;
    private final Settings                   mSettings;
    private       String                     mEulaText;
    @Nullable
    private SdkFeature.TabCompletionCallback mCompletionCallback;

    public EulaModelImpl(@NonNull ThreadManager    threadManager,
                         @NonNull ExecutorService  executorService,
                         @NonNull EulaTasksFactory tasksFactory,
                         @NonNull Settings         settings) {
        mThreadManager   = threadManager;
        mExecutorService = executorService;
        mTasksFactory    = tasksFactory;
        mSettings        = settings;
    }

    @Override
    public void loadEula() {
        if (mEulaText == null) {
            mExecutorService.execute(mTasksFactory.createLoadEulaTask(mThreadManager, this));
        } else {
            onEulaLoaded(mEulaText);
        }
    }

    @Override
    public void acceptEula() {
        mEulaText = null;
        mSettings.setEulaAccepted(true);
        if (mCompletionCallback != null) {
            mCompletionCallback.onTabResult(SdkFeature.TabResult.Accept);
        }
    }

    @Override
    public void rejectEula() {
        mEulaText = null;
        mSettings.setEulaAccepted(false);
        if (mCompletionCallback != null) {
            mCompletionCallback.onTabResult(SdkFeature.TabResult.Reject);
        }
    }


    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void onEulaLoaded(@NonNull String eulaText) {
        notifyObservers(observer -> observer.onEulaLoaded(eulaText), EulaModelStatusObserver.class);
    }

    @Override
    public void setTabCompletionCallback(@Nullable SdkFeature.TabCompletionCallback callback) {
        mCompletionCallback = callback;
    }
}
