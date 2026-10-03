/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.database.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.view.database.DbViewState;

public final class DbViewStateImpl implements DbViewState {
    @NonNull private final String  mDbPath;
    @NonNull private final String  mPassword;
    @NonNull private final String  mSqlRequest;
    @NonNull private final String  mSqlResult;

    public DbViewStateImpl(@NonNull String dbPath,
                           @NonNull String password,
                           @NonNull String request,
                           @NonNull String result) {
        mDbPath         = dbPath;
        mPassword       = password;
        mSqlRequest     = request;
        mSqlResult      = result;
    }

    @Override
    @NonNull
    public String getDbPath() {
        return mDbPath;
    }

    @Override
    @NonNull
    public String getPassword() {
        return mPassword;
    }

    @Override
    @NonNull
    public String getSqlRequest() {
        return mSqlRequest;
    }

    @Override
    @NonNull
    public String getSqlResult() {
        return mSqlResult;
    }

}
