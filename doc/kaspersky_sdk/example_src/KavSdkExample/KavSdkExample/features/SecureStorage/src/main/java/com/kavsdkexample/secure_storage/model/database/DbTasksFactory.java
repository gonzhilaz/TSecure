/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.database;

import androidx.annotation.NonNull;

import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.SecureStorageBaseTasksFactory;

public interface DbTasksFactory extends SecureStorageBaseTasksFactory {
    @NonNull
    Runnable createOpenDatabaseTask(@NonNull ThreadManager     threadManager,
                                    @NonNull DbResultsObserver observer,
                                    @NonNull String            path,
                                    @NonNull String            password);

    @NonNull
    Runnable createCloseDatabaseTask(@NonNull ThreadManager          threadManager,
                                     @NonNull SQLiteDatabase         db,
                                     @NonNull CloseDbResultsObserver observer,
                                     boolean                         removeAfterClose);

    @NonNull
    Runnable createRequestDatabaseTask(@NonNull ThreadManager            threadManager,
                                       @NonNull SQLiteDatabase           db,
                                       @NonNull RequestDbResultsObserver observer,
                                       @NonNull String                   request);
}
