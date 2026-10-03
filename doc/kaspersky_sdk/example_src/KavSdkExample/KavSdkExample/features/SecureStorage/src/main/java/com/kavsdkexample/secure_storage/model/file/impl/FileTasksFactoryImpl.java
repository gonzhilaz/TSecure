/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.file.FileTasksFactory;
import com.kavsdkexample.secure_storage.model.file.ReadFileResultsObserver;
import com.kavsdkexample.secure_storage.model.file.WriteFileResultsObserver;

public final class FileTasksFactoryImpl implements FileTasksFactory {
    private final Context mContext;

    public FileTasksFactoryImpl(@NonNull Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public Runnable createReadFileTask(@NonNull ThreadManager           threadManager,
                                       @NonNull ReadFileResultsObserver observer,
                                       @NonNull String                  path,
                                       @NonNull String                  password,
                                                int                     pageNum) {
        return new ReadFileTask(mContext, threadManager, observer, path, password, pageNum);
    }

    @Override
    @NonNull
    public Runnable createWriteFileTask(@NonNull ThreadManager            threadManager,
                                        @NonNull WriteFileResultsObserver observer,
                                        @NonNull String                   path,
                                        @NonNull String                   password,
                                        @NonNull String                   content,
                                                 boolean                  append) {
        return new WriteFileTask(mContext, threadManager, observer, path, password, content, append);
    }
}
