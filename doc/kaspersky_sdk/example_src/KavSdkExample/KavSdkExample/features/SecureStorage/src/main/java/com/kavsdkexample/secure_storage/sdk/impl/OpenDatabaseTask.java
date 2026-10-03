/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.sdk.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import android.util.Log;

import com.kavsdk.license.SdkLicenseViolationException;
import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdk.securestorage.database.SQLiteException;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.BuildConfig;
import com.kavsdkexample.secure_storage.model.database.DbResultsObserver;

public final class OpenDatabaseTask extends BaseSecureStorageTask<DbResultsObserver> {
    private static final String TAG = OpenDatabaseTask.class.getSimpleName();

    OpenDatabaseTask(@NonNull Context context,
                     @NonNull ThreadManager threadManager,
                     @NonNull DbResultsObserver observer,
                     @NonNull String path,
                     @NonNull String password) {
        super(context, threadManager, observer, path, password);
    }

    @Override
    @WorkerThread
    public void run() {
        SQLiteDatabase database;

        try {
            database = SQLiteDatabase.openOrCreateDatabase(mPath, mPassword, null);
        } catch (final SdkLicenseViolationException | SQLiteException e) {
            notifyError(e);
            return;
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Database is opened. Version: " + database.getVersion());
        }

        notifyObserver(database);
    }

    private void notifyObserver(@NonNull final SQLiteDatabase database) {
        final DbResultsObserver observer = getObserver();
        if (observer != null) {
            mThreadManager.runOnUiThread(() -> observer.onDbOpened(database));
        }
    }
}
