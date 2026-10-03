/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.base.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.BaseResultsObserver;

import java.lang.ref.WeakReference;

public abstract class BaseSecureStorageTask<T extends BaseResultsObserver> implements Runnable {
    protected final Context          mContext;
    protected final ThreadManager    mThreadManager;
    private   final WeakReference<T> mResultsObserverRef;

    protected BaseSecureStorageTask(@NonNull Context       context,
                                    @NonNull ThreadManager threadManager,
                                    @NonNull T             observer) {
        mContext            = context;
        mThreadManager      = threadManager;
        mResultsObserverRef = new WeakReference<>(observer);
    }


    protected T getObserver() {
        return mResultsObserverRef.get();
    }

    @WorkerThread
    protected void notifyError(@NonNull final Exception exception) {
        final BaseResultsObserver observer = mResultsObserverRef.get();
        if (observer != null) {
            mThreadManager.runOnUiThread(() -> observer.onError(exception.getMessage()));
        }
    }

    @WorkerThread
    protected void notifyError(@NonNull final String message) {
        final BaseResultsObserver observer = mResultsObserverRef.get();
        if (observer != null) {
            mThreadManager.runOnUiThread(() -> observer.onError(message));
        }
    }
}
