/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.file;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;

@UiThread
public interface FileView extends SecureStorageBaseView {
    @NonNull
    String getFileContent();
    void   setFileContent(@NonNull String result, int pageNum, int pageCount);
    void   showFileWritten();
    void   showOverwriteDialog(boolean append);
}

