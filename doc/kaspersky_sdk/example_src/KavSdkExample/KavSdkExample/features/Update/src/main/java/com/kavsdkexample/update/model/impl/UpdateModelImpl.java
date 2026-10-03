/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.update.model.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.update.model.UpdateController;
import com.kavsdkexample.update.model.UpdateModel;
import com.kavsdkexample.update.model.Settings;
import com.kavsdkexample.update.model.UpdateModelComponentMode;
import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.update.model.UpdateModelObserver;
import com.kavsdkexample.update.model.UpdateModelUpdateServerMode;
import com.kavsdkexample.update.model.UpdateResults;
import com.kavsdkexample.update.model.UpdateTaskFactory;

import java.util.concurrent.ExecutorService;

public class UpdateModelImpl extends BaseModelImpl implements UpdateModel, UpdateModelObserver {

    private final Settings mSettings;
    private final ExecutorService mExecutor;
    private final UpdateTaskFactory mUpdateTaskFactory;
    private final ThreadManager mThreadManager;
    private UpdateController mController;
    private boolean mIsStarting;
    private boolean mIsStarted;

    public UpdateModelImpl(@NonNull ThreadManager threadManager,
                           @NonNull UpdateTaskFactory updateTaskFactory,
                           @NonNull ExecutorService executorService,
                           @NonNull Settings settings) {
        mThreadManager = threadManager;
        mUpdateTaskFactory = updateTaskFactory;
        mExecutor = executorService;
        mSettings = settings;
        mIsStarting = false;
        mIsStarted = false;
    }

    @Override
    @NonNull
    public UpdateModelComponentMode getUpdateModelComponent() {
        return mSettings.getUpdateModelComponent();
    }

    @Override
    public void setUpdateModelComponent(@NonNull UpdateModelComponentMode component) {
        mSettings.setUpdateModelComponent(component);
    }

    @Override
    @NonNull
    public UpdateModelUpdateServerMode getUpdateModelUpdateServer() {
        return mSettings.getUpdateModelUpdateServer();
    }

    @Override
    public void setUpdateModelUpdateServer(@NonNull UpdateModelUpdateServerMode updateServer) {
        mSettings.setUpdateModelUpdateServer(updateServer);
    }

    @Override
    public @NonNull
    String getUpdateServer() {
        return mSettings.getUpdateServer();
    }

    @Override
    public void setUpdateServer(@NonNull String updateServer) {
        mSettings.setUpdateServer(updateServer);
    }

    @Override
    public void updateRequest() {
        if (!mIsStarting) {
            if (!mIsStarted) {
                mIsStarting = true;
            } else {
                if (mController != null) {
                    mController.cancel();
                }
                return;
            }
        } else {
            return;
        }

        mExecutor.execute(mUpdateTaskFactory.createUpdateTask(mThreadManager, this, mSettings));
    }

    @Override
    public void updateIsCompleted() {
        notifyObservers(UpdateModelObserver::updateIsCompleted, UpdateModelObserver.class);

        mIsStarting = false;
        mIsStarted = false;
    }

    @Override
    public void executionDetails(UpdateResults result) {
        notifyObservers(observer -> observer.executionDetails(result), UpdateModelObserver.class);
    }

    @Override
    public void executionDetails(UpdateResults result, int code) {
        notifyObservers(observer -> observer.executionDetails(result, code), UpdateModelObserver.class);
    }

    @Override
    public void executionDetails(UpdateResults result, final String details) {
        notifyObservers(observer -> observer.executionDetails(result, details), UpdateModelObserver.class);
    }

    @Override
    public void updateIsRunning() {
        notifyObservers(UpdateModelObserver::updateIsRunning, UpdateModelObserver.class);

        mIsStarting = false;
        mIsStarted = true;
    }

    @Override
    public void setController(UpdateController controller) {
        mController = controller;
    }
}