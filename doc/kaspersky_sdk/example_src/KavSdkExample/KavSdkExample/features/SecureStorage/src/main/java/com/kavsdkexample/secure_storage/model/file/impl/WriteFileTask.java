/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.kavsdk.securestorage.file.CryptoFileOutputStream;
import com.kavsdkexample.core.app.utils.IoUtils;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.sdk.impl.BaseSecureStorageTask;
import com.kavsdkexample.secure_storage.model.file.WriteFileResultsObserver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class WriteFileTask extends BaseSecureStorageTask<WriteFileResultsObserver> {
    private final boolean mAppend;
    private final String  mContent;

    WriteFileTask(@NonNull Context                  context,
                  @NonNull ThreadManager            threadManager,
                  @NonNull WriteFileResultsObserver observer,
                  @NonNull String                   path,
                  @NonNull String                   password,
                  @NonNull String                   content,
                           boolean                  append) {
        super(context, threadManager, observer, path, password);
        mContent        = content;
        mAppend         = append;
    }

    @Override
    @WorkerThread
    public void run() {
        CryptoFileOutputStream out = null;

        try {
            out = new CryptoFileOutputStream(mPath, mAppend, mPassword);
            out.write(mContent.getBytes(Charset.defaultCharset()));

        } catch (IOException e) {
            notifyError(e.getMessage());
            return;
        } finally {
            IoUtils.closeQuietly(out);
        }
        WriteFileResultsObserver observer = getObserver();
        if (observer != null) {
            mThreadManager.runOnUiThread(()->observer.onFileWritten(new File(mPath)));
        }
    }
}
