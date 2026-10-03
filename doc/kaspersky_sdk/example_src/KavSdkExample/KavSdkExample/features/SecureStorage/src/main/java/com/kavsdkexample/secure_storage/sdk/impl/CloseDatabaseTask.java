/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.R;
import com.kavsdkexample.secure_storage.model.base.impl.BaseSecureStorageTask;
import com.kavsdkexample.secure_storage.model.database.CloseDbResultsObserver;

import java.io.File;

public final class CloseDatabaseTask extends BaseSecureStorageTask<CloseDbResultsObserver> {
    private final SQLiteDatabase mDatabase;
    private final boolean        mRemoveDb;

    CloseDatabaseTask(@NonNull Context                context,
                      @NonNull ThreadManager          threadManager,
                      @NonNull SQLiteDatabase         db,
                      @NonNull CloseDbResultsObserver observer,
                      boolean                         removeAfterClose) {
        super(context, threadManager, observer);
        mDatabase = db;
        mRemoveDb = removeAfterClose;
    }

    @Override
    @WorkerThread
    public void run() {
        String path = mDatabase.getPath();
        mDatabase.close();

        if (mRemoveDb) {
            String pathCrypto  = path + "-crypto";
            boolean resultDb   = new File(path).delete();
            boolean resultHash = new File(pathCrypto).delete();
            if (!resultDb) {
                notifyError(mContext.getString(R.string.str_secure_storage_test_database_error_delete_failed, path));
            } else if (!resultHash) {
                notifyError(mContext.getString(R.string.str_secure_storage_test_database_error_delete_failed, pathCrypto));
            }
        }

        notifyDbClosed(path, mRemoveDb);
    }

    private void notifyDbClosed(@NonNull final String path, boolean removeDb) {
        CloseDbResultsObserver observer = getObserver();
        if (observer != null) {
            mThreadManager.runOnUiThread(() -> observer.onDbClosed(path, removeDb));
        }
    }
}
