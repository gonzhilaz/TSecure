/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.base.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.presenter.base.SecureStorageAsyncOperationsObserver;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;

public class SecureStorageAsyncOperationsObserverImpl<T extends SecureStorageBaseView> implements SecureStorageAsyncOperationsObserver {
    protected final T mView;

    protected SecureStorageAsyncOperationsObserverImpl(@NonNull T view) {
        mView = view;
    }

    @Override
    public void onError(@NonNull String error) {
        mView.setOperationsButtonsEnabled(true);
        mView.showError(error);
    }

    @Override
    public void onError(@NonNull SecureStorageBaseView.ErrorType errorType) {
        mView.setOperationsButtonsEnabled(true);
        mView.showError(errorType);
    }
}
