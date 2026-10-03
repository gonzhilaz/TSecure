/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.base.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.model.impl.BaseModelImpl;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.BaseResultsObserver;
import com.kavsdkexample.secure_storage.model.base.SecureStorageBaseModel;
import com.kavsdkexample.secure_storage.model.base.SecureStorageBaseTasksFactory;
import com.kavsdkexample.secure_storage.presenter.base.SecureStorageAsyncOperationsObserver;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;

import java.util.concurrent.ExecutorService;

public class SecureStorageBaseModelImpl<F extends    SecureStorageBaseTasksFactory>
                                          extends    BaseModelImpl
                                          implements SecureStorageBaseModel,
                                                     BaseResultsObserver {
    protected final ThreadManager   mThreadManager;
    protected final ExecutorService mExecutor;
    protected final F               mTasksFactory;

    public SecureStorageBaseModelImpl(@NonNull ThreadManager   threadManager,
                                      @NonNull ExecutorService executorService,
                                      @NonNull F               tasksFactory) {
        mThreadManager = threadManager;
        mExecutor      = executorService;
        mTasksFactory  = tasksFactory;
    }

    @Override
    public void onError(@NonNull String message) {
        notifyError(message);
    }

    protected void notifyError(@NonNull String error) {
        notifyObservers(observer -> observer.onError(error), SecureStorageAsyncOperationsObserver.class);
    }

    protected void notifyError(@NonNull SecureStorageBaseView.ErrorType error) {
        notifyObservers(observer -> observer.onError(error), SecureStorageAsyncOperationsObserver.class);
    }
}
