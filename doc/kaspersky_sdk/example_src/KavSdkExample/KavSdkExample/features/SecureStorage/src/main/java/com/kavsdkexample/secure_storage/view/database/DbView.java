/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.database;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;

@UiThread
public interface DbView extends SecureStorageBaseView {
    void setSqlRequest(@NonNull String request);
    void setSqlResult(@NonNull String sqlResult);
    void setDatabaseOpened(boolean opened);
}

