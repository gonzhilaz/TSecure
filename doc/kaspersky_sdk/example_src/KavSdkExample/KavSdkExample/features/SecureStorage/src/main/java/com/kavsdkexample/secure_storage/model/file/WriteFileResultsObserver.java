/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.model.base.BaseResultsObserver;

import java.io.File;

@UiThread
public interface WriteFileResultsObserver extends BaseResultsObserver {
    void onFileWritten(@NonNull File path);
}
