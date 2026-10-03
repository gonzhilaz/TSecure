/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.sdk.impl;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.kavsdk.securestorage.database.AbstractCursor;
import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdk.securestorage.database.util.DatabaseUtils;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.impl.BaseSecureStorageTask;
import com.kavsdkexample.secure_storage.model.database.RequestDbResultsObserver;

public final class RequestDatabaseTask extends BaseSecureStorageTask<RequestDbResultsObserver> {
    /* A maximum number of rows in table to show */
    private static final int MAX_ROWS = 30;
    private final SQLiteDatabase mDatabase;
    private final String mRequest;

    RequestDatabaseTask(@NonNull Context context,
                        @NonNull ThreadManager threadManager,
                        @NonNull SQLiteDatabase db,
                        @NonNull String request,
                        @NonNull RequestDbResultsObserver observer) {
        super(context, threadManager, observer);
        mDatabase = db;
        mRequest  = request;
    }

    @Override
    @WorkerThread
    public void run() {
        StringBuilder builder = new StringBuilder();
        builder.append("--- EXECUTE ---\n\n");
        final String trimmedSql = mRequest.trim();
        try {
            int n = DatabaseUtils.getSqlStatementType(trimmedSql);

            if (n == DatabaseUtils.STATEMENT_SELECT) {
                executeSelectRequest(trimmedSql, builder);
            } else {
                mDatabase.execSQL(trimmedSql);
                builder.append("<no output>\n");
            }
        } catch (Exception e) {
            notifyError(e);
            return;
        }
        builder.append("\n--- EXECUTE END ---\n");
        notifyRequestCompleted(builder.toString());
    }

    private void executeSelectRequest(@NonNull String trimmedSql, @NonNull StringBuilder output) {
        try (AbstractCursor cursor = (AbstractCursor) mDatabase.rawQuery(trimmedSql, null)) {

            final int columnCount = cursor.getColumnCount();

            output.append("|  ");
            for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
                output.append(cursor.getColumnName(columnIndex));
                output.append("  |  ");
            }
            output.append("\n-------------------\n");

            int rowIndex = 0;
            while (cursor.moveToNext()) {
                if (rowIndex >= MAX_ROWS) {
                    output.append("...\n")
                            .append("TOP ")
                            .append(MAX_ROWS)
                            .append(" PRINTED\n");
                    break;
                }
                output.append(" | ");
                for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
                    int type = cursor.getType(columnIndex);
                    switch (type) {
                        case Cursor.FIELD_TYPE_NULL:
                            output.append("<null>");
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            output.append(cursor.getLong(columnIndex));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            output.append(cursor.getDouble(columnIndex));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            output.append(cursor.getString(columnIndex));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            output.append("<blob>");
                            break;
                        default:
                            break;
                    }
                    output.append(" | ");
                    ++rowIndex;
                }
                output.append('\n');
            }
        }
    }

    private void notifyRequestCompleted(String result) {
        final RequestDbResultsObserver observer = getObserver();
        if (observer != null) {
            mThreadManager.runOnUiThread(() -> observer.onRequestCompleted(result));
        }
    }
}
