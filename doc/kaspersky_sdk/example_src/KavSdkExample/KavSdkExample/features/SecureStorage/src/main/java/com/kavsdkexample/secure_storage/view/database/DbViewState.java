/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.database;

import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.view.SecureStorageBaseViewState;

public interface DbViewState extends SecureStorageBaseViewState {
    @NonNull   String  getDbPath();
    @NonNull   String  getSqlRequest();
    @NonNull   String  getSqlResult();
}
