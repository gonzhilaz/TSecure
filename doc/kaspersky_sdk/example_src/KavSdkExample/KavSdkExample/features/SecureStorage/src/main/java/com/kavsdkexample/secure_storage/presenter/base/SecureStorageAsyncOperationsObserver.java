/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.base;

import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;

public interface SecureStorageAsyncOperationsObserver {

    void onError(@NonNull String error);
    void onError(@NonNull SecureStorageBaseView.ErrorType errorType);
}
