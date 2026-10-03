/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.database.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.model.database.DbModel;
import com.kavsdkexample.secure_storage.presenter.database.DbAsyncOperationsObserver;
import com.kavsdkexample.secure_storage.presenter.database.DbPresenter;
import com.kavsdkexample.secure_storage.presenter.base.impl.SecureStorageAsyncOperationsObserverImpl;
import com.kavsdkexample.secure_storage.presenter.base.impl.SecureStoragePresenterImpl;
import com.kavsdkexample.secure_storage.view.database.DbView;
import com.kavsdkexample.secure_storage.view.database.DbViewState;
import com.kavsdk.securestorage.database.SQLiteDatabase;

import javax.inject.Inject;

@UiThread
public final class DbPresenterImpl extends SecureStoragePresenterImpl<DbView, DbViewState, DbModel>
                                   implements DbPresenter {
    @Inject
    DbPresenterImpl(@NonNull DbModel dbModel) {
        super(dbModel);
    }

    @Override
    public void openDatabase(@NonNull String path, @NonNull String password) {
        mModel.openDatabase(path, password);
    }

    @Override
    @NonNull
    public String getDatabaseFilePath() {
        return mModel.getDatabaseFilePath();
    }

    @Override
    public void createTestTable() {
        if (mView != null) {
            mView.setOperationsButtonsEnabled(false);
        }
        mModel.createTestTable();
    }

    @Override
    public void closeDatabase(boolean removeAfterClose) {
        mModel.closeDatabase(removeAfterClose);
    }

    @Override
    public void executeDatabaseRequest(@NonNull String sqlRequest) {
        if (mView != null) {
            mView.setOperationsButtonsEnabled(false);
        }
        mModel.executeDatabaseRequest(sqlRequest);
    }

    @Override
    public void subscribe(@NonNull DbView view, @Nullable DbViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new DbAsyncOperationsObserverImpl(view));

        if (viewCreated) {
            if (state == null) {
                SQLiteDatabase db = mModel.getDatabase();
                boolean dbOpened = (db != null);
                view.setDatabaseOpened(dbOpened);
                if (dbOpened) {
                    view.setPath(mModel.getDatabaseFilePath());
                }
            } else {
                view.setDatabaseOpened(mModel.getDatabase() != null);
                view.setPath(state.getDbPath());
                view.setSqlRequest(state.getSqlRequest());
                view.setSqlResult(state.getSqlResult());
            }
        }
    }

    @UiThread
    private static class DbAsyncOperationsObserverImpl extends SecureStorageAsyncOperationsObserverImpl<DbView>
                                                       implements DbAsyncOperationsObserver {
        DbAsyncOperationsObserverImpl(@NonNull DbView view) {
            super(view);
        }
        @Override
        public void onDatabaseOpened() {
            mView.setOperationsButtonsEnabled(true);
            mView.setDatabaseOpened(true);
        }

        @Override
        public void onDatabaseClosed(@NonNull String path, boolean dbFileRemoved) {
            mView.setOperationsButtonsEnabled(true);
            mView.setDatabaseOpened(false);
        }

        @Override
        public void onDatabaseRequestCompleted(@NonNull String result) {
            mView.setOperationsButtonsEnabled(true);
            mView.setSqlResult(result);
        }
    }
}
