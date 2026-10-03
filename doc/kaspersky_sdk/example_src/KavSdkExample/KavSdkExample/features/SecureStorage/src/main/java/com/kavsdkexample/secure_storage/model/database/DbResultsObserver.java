/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.database;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdkexample.secure_storage.model.base.BaseResultsObserver;

public interface DbResultsObserver extends BaseResultsObserver {
    @UiThread
    void onDbOpened(@NonNull SQLiteDatabase database);
}
