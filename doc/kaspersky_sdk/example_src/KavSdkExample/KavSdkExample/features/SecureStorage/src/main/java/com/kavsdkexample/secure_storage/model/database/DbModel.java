/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.database;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdk.securestorage.database.SQLiteDatabase;
import com.kavsdkexample.secure_storage.model.base.SecureStorageBaseModel;

@UiThread
public interface DbModel extends SecureStorageBaseModel,
                                 DatabaseOperations {
    @Nullable
    SQLiteDatabase getDatabase();
}
