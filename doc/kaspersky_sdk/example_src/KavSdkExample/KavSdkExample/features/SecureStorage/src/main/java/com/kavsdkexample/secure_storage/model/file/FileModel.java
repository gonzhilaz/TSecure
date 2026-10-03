/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.kavsdkexample.secure_storage.model.base.SecureStorageBaseModel;

import java.io.File;

@UiThread
public interface FileModel extends SecureStorageBaseModel, FileOperations {

    @Nullable File getCurrentFilePath();
}
