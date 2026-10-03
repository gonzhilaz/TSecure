/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;

import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.database.CloseDbResultsObserver;
import com.kavsdkexample.secure_storage.model.database.DbResultsObserver;
import com.kavsdkexample.secure_storage.model.database.DbTasksFactory;
import com.kavsdkexample.secure_storage.model.database.RequestDbResultsObserver;

public final class DbTasksFactoryImpl implements DbTasksFactory {
    private final Context mContext;

    public DbTasksFactoryImpl(@NonNull Context context) {
        mContext = context;
    }

    @Override
    @NonNull
    public Runnable createOpenDatabaseTask(@NonNull ThreadManager     threadManager,
                                           @NonNull DbResultsObserver observer,
                                           @NonNull String            path,
                                           @NonNull String            password) {
        return new OpenDatabaseTask(mContext, threadManager, observer, path, password);

    }

    @Override
    @NonNull
    public Runnable createCloseDatabaseTask(@NonNull ThreadManager          threadManager,
                                            @NonNull SQLiteDatabase         db,
                                            @NonNull CloseDbResultsObserver observer,
                                            boolean                         removeAfterClose) {
        return new CloseDatabaseTask(mContext, threadManager, db, observer, removeAfterClose);
    }

    @Override
    @NonNull
    public Runnable createRequestDatabaseTask(@NonNull ThreadManager            threadManager,
                                              @NonNull SQLiteDatabase           db,
                                              @NonNull RequestDbResultsObserver observer,
                                              @NonNull String                   request) {
        return new RequestDatabaseTask(mContext, threadManager, db, request, observer);
    }
}
