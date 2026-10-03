/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.BaseResultsObserver;

public abstract class BaseSecureStorageTask<T extends BaseResultsObserver>
        extends com.kavsdkexample.secure_storage.model.base.impl.BaseSecureStorageTask<T> {
    protected final String mPath;
    protected final String mPassword;

    protected BaseSecureStorageTask(@NonNull Context       context,
                                    @NonNull ThreadManager threadManager,
                                    @NonNull T             observer,
                                    @NonNull String        path,
                                    @NonNull String        password) {
        super(context, threadManager, observer);
        mPath     = path;
        mPassword = password;
    }
}
