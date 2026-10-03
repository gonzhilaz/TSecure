/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.database;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.presenter.base.SecureStorageAsyncOperationsObserver;

@UiThread
public interface DbAsyncOperationsObserver extends SecureStorageAsyncOperationsObserver {
    void onDatabaseOpened();
    void onDatabaseClosed(@NonNull String path, boolean dbFileRemoved);
    void onDatabaseRequestCompleted(@NonNull String result);
}
