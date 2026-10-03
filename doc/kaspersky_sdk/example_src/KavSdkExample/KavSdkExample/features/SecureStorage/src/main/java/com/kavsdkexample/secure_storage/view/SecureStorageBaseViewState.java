/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.core.app.view.BaseViewState;

@UiThread
public interface SecureStorageBaseViewState extends BaseViewState {
    @NonNull   String  getPassword();
}
