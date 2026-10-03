/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.SecureStorageBaseTasksFactory;

public interface FileTasksFactory extends SecureStorageBaseTasksFactory {
    @NonNull
    Runnable createReadFileTask(@NonNull ThreadManager           threadManager,
                                @NonNull ReadFileResultsObserver observer,
                                @NonNull String                  path,
                                @NonNull String                  password,
                                         int                     pageNum);

    @NonNull
    Runnable createWriteFileTask(@NonNull ThreadManager            threadManager,
                                 @NonNull WriteFileResultsObserver observer,
                                 @NonNull String                   path,
                                 @NonNull String                   password,
                                 @NonNull String                   content,
                                          boolean                  append);
}
