/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.view.BaseView;

@UiThread
public interface SecureStorageBaseView extends BaseView {
    @NonNull String  getPath();
    @NonNull String  getPassword();
             void    setOperationsButtonsEnabled(boolean enabled);
             void    setPath(@NonNull String path);
             void    setPassword(@NonNull String password);
             void    showError(@NonNull String error);
             void    showError(@NonNull ErrorType errorType);

    enum ErrorType {
        EmptyData,
        EmptyPath,
        EmptyPassword,
        DbNotOpened,
        DbAlreadyOpened
    }
}
