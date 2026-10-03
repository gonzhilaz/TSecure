/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.kavsdk.securestorage.file.CryptoFileInputStream;
import com.kavsdkexample.core.app.utils.IoUtils;
import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.sdk.impl.BaseSecureStorageTask;
import com.kavsdkexample.secure_storage.model.file.ReadFileResultsObserver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ReadFileTask extends BaseSecureStorageTask<ReadFileResultsObserver> {
    private static final int MAX_EDIT_FILESIZE = 20 * 1024; //Maximum file size that is available for editing
    private final int mPageNum;

    ReadFileTask(@NonNull Context                 context,
                 @NonNull ThreadManager           threadManager,
                 @NonNull ReadFileResultsObserver observer,
                 @NonNull String                  path,
                 @NonNull String                  password,
                          int                     pageNum) {
          super(context, threadManager, observer, path, password);
          mPageNum = pageNum;
    }

    @Override
    @WorkerThread
    public void run() {
        String result;

        CryptoFileInputStream in = null;
        int countOfPages;

        try {
            in = new CryptoFileInputStream(mPath, mPassword);
            int textlen = in.available();
            countOfPages = (textlen + MAX_EDIT_FILESIZE - 1) / MAX_EDIT_FILESIZE;

            if (countOfPages > 1) {
                if (mPageNum < countOfPages - 1) {
                    textlen = MAX_EDIT_FILESIZE;
                } else {
                    textlen =  textlen - (countOfPages - 1) * MAX_EDIT_FILESIZE;
                }
            }

            byte[] buffer = new byte[textlen];

            //noinspection ResultOfMethodCallIgnored
            in.skip(mPageNum * MAX_EDIT_FILESIZE);
            //noinspection ResultOfMethodCallIgnored
            in.read(buffer, 0, textlen);

            result = new String(buffer, Charset.defaultCharset());

        } catch (IOException e) {
            notifyError(e.getMessage());
            return;
        } finally {
            IoUtils.closeQuietly(in);
        }
        ReadFileResultsObserver observer = getObserver();
        if (observer != null) {
            mThreadManager.runOnUiThread(()->observer.onFileRead(new File(mPath), result, mPageNum, countOfPages));
        }
    }
}
