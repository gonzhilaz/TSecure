/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.database.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import android.text.TextUtils;

import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdkexample.core.app.utils.CompoundRunnable;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.impl.SecureStorageBaseModelImpl;
import com.kavsdkexample.secure_storage.model.database.CloseDbResultsObserver;
import com.kavsdkexample.secure_storage.model.database.DbModel;
import com.kavsdkexample.secure_storage.model.database.DbResultsObserver;
import com.kavsdkexample.secure_storage.model.database.DbTasksFactory;
import com.kavsdkexample.secure_storage.model.database.RequestDbResultsObserver;
import com.kavsdkexample.secure_storage.presenter.database.DbAsyncOperationsObserver;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;

import java.util.concurrent.ExecutorService;

@UiThread
public final class DbModelImpl extends    SecureStorageBaseModelImpl<DbTasksFactory>
                               implements DbModel,
                                          DbResultsObserver,
                                          CloseDbResultsObserver,
                                          RequestDbResultsObserver {

    @Nullable
    private SQLiteDatabase            mDatabase;

    public DbModelImpl(@NonNull ThreadManager     threadManager,
                       @NonNull ExecutorService   executorService,
                       @NonNull DbTasksFactory    tasksFactory) {
        super(threadManager, executorService, tasksFactory);
    }

    @Nullable
    @Override
    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    @Override
    public void openDatabase(@NonNull String path, @NonNull String password) {
        if (TextUtils.isEmpty(path)) {
            notifyError(SecureStorageBaseView.ErrorType.EmptyPath);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            notifyError(SecureStorageBaseView.ErrorType.EmptyPassword);
            return;
        }
        Runnable openDbTask = mTasksFactory.createOpenDatabaseTask(mThreadManager, this, path, password);
        if (mDatabase != null) {
            if (path.equals(mDatabase.getPath())) {
                notifyError(SecureStorageBaseView.ErrorType.DbAlreadyOpened);
                return;
            }
            Runnable closeDbTask = mTasksFactory.createCloseDatabaseTask(mThreadManager, mDatabase, this, false);
            openDbTask = new CompoundRunnable(closeDbTask, openDbTask);
        }
        mExecutor.execute(openDbTask);
    }

    @Override
    @NonNull
    public String getDatabaseFilePath() {
        return mDatabase == null ? "" : mDatabase.getPath();
    }

    @Override
    public void createTestTable() {
        if (mDatabase == null) {
            notifyError(SecureStorageBaseView.ErrorType.DbNotOpened);
            return;
        }

        String createTable =
                "CREATE TABLE test_table\n" +
                        "( id INTEGER PRIMARY KEY,\n" +
                        "  string_field VARCHAR NOT NULL,\n" +
                        "  int_field INTEGER NOT NULL DEFAULT 0\n" +
                        ");";
        String insert = "INSERT INTO test_table (\n" +
                " id,\n" +
                " string_field,\n" +
                " int_field)\n" +
                "VALUES\n" +
                " (\n" +
                " 0,\n" +
                " 'sample string',\n" +
                " 1);";
        mExecutor.execute(new CompoundRunnable(
                mTasksFactory.createRequestDatabaseTask(mThreadManager, mDatabase, this, createTable),
                mTasksFactory.createRequestDatabaseTask(mThreadManager, mDatabase, this, insert)
        ));
    }

    @Override
    public void closeDatabase(boolean removeAfterClose) {
        if (mDatabase == null) {
            notifyError(SecureStorageBaseView.ErrorType.DbNotOpened);
            return;
        }
        mExecutor.execute(mTasksFactory.createCloseDatabaseTask(mThreadManager, mDatabase, this, removeAfterClose));
    }

    @Override
    public void executeDatabaseRequest(@NonNull String sqlRequest) {
        if (mDatabase == null) {
            notifyError(SecureStorageBaseView.ErrorType.DbNotOpened);
            return;
        }
        mExecutor.execute(mTasksFactory.createRequestDatabaseTask(mThreadManager, mDatabase, this, sqlRequest));
    }

    @Override
    public void onDbOpened(@NonNull SQLiteDatabase database) {
        mDatabase = database;
        notifyObservers(DbAsyncOperationsObserver::onDatabaseOpened, DbAsyncOperationsObserver.class);
    }

    @Override
    public void onDbClosed(@NonNull String path, boolean dbFileRemoved) {
        mDatabase = null;
        notifyObservers(observer -> observer.onDatabaseClosed(path, dbFileRemoved), DbAsyncOperationsObserver.class);
    }

    @Override
    public void onRequestCompleted(@NonNull String result) {
        notifyObservers(observer -> observer.onDatabaseRequestCompleted(result), DbAsyncOperationsObserver.class);
    }
}
