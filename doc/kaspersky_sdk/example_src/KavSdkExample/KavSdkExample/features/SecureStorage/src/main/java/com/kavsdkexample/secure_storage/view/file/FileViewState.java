/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.file;

import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.view.SecureStorageBaseViewState;

public interface FileViewState extends SecureStorageBaseViewState {
    @NonNull   String  getFilePath();
    @NonNull   String  getFileContent();
               int     getCurrentFilePage();
               int     getFilePagesCount();
}
