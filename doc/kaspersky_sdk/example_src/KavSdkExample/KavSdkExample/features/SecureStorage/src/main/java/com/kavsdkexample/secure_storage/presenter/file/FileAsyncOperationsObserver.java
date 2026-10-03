/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.file;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.presenter.base.SecureStorageAsyncOperationsObserver;

@UiThread
public interface FileAsyncOperationsObserver extends SecureStorageAsyncOperationsObserver {
    void onFileReaded(@NonNull String result, int currentPageNum, int pagesNum);
    void onFileWritten();
}
