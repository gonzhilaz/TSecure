/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.database.impl;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.view.database.DbViewState;

public final class DbViewHelper {
    private static final String DB_PATH_KEY         = "db_path";
    private static final String PASSWORD_KEY        = "db_password";
    private static final String SQL_REQUEST_KEY     = "sql_request";
    private static final String SQL_RESULT_KEY      = "sql_result";

    private DbViewHelper() {
    }

    @NonNull
    public static DbViewState fromBundle(@NonNull Bundle bundle) {
        return new DbViewStateImpl(
                bundle.getString (DB_PATH_KEY         ,    ""),
                bundle.getString (PASSWORD_KEY        ,    ""),
                bundle.getString (SQL_REQUEST_KEY     ,    ""),
                bundle.getString (SQL_RESULT_KEY      ,    "")
        );
    }

    public static void toBundle(@NonNull Bundle bundle, @NonNull DbViewState state) {
        bundle.putString (DB_PATH_KEY         , state.getDbPath()              );
        bundle.putString (PASSWORD_KEY        , state.getPassword()            );
        bundle.putString (SQL_REQUEST_KEY     , state.getSqlRequest()          );
        bundle.putString (SQL_RESULT_KEY      , state.getSqlResult()           );
    }
}
