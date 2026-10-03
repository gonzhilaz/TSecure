/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.model.file.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.kavsdkexample.core.app.utils.ThreadManager;
import com.kavsdkexample.secure_storage.model.base.impl.SecureStorageBaseModelImpl;
import com.kavsdkexample.secure_storage.model.file.FileModel;
import com.kavsdkexample.secure_storage.model.file.FileTasksFactory;
import com.kavsdkexample.secure_storage.model.file.ReadFileResultsObserver;
import com.kavsdkexample.secure_storage.model.file.WriteFileResultsObserver;
import com.kavsdkexample.secure_storage.presenter.file.FileAsyncOperationsObserver;
import com.kavsdkexample.secure_storage.view.SecureStorageBaseView;

import java.io.File;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

public class FileModelImpl extends    SecureStorageBaseModelImpl<FileTasksFactory>
                           implements FileModel,
                                      ReadFileResultsObserver,
                                      WriteFileResultsObserver {
    @Nullable private File mCurrentFilePath;

    @Inject
    public FileModelImpl(@NonNull ThreadManager    threadManager,
                         @NonNull ExecutorService  executorService,
                         @NonNull FileTasksFactory tasksFactory) {
        super(threadManager, executorService, tasksFactory);
    }

    @Override
    @Nullable
    public File getCurrentFilePath() {
        return mCurrentFilePath;
    }

    @Override
    public void readFile(@NonNull String path, @NonNull String password, int pageNum) {
        if (checkPathAndPasswordInvalid(path, password)) {
            return;
        }
        mExecutor.execute(mTasksFactory.createReadFileTask(mThreadManager, this, path, password, pageNum));
    }

    @Override
    public void writeFile(@NonNull String path, @NonNull String password, String content, boolean append) {
        if (checkPathAndPasswordInvalid(path, password)) {
            return;
        }
        mExecutor.execute(mTasksFactory.createWriteFileTask(mThreadManager, this, path, password, content, append));
    }

    private boolean checkPathAndPasswordInvalid(@NonNull String path, @NonNull String password) {
        if (TextUtils.isEmpty(path)) {
            notifyError(SecureStorageBaseView.ErrorType.EmptyPath);
            return true;
        }
        if (TextUtils.isEmpty(password)) {
            notifyError(SecureStorageBaseView.ErrorType.EmptyPassword);
            return true;
        }
        return false;
    }

    @Override
    public void onFileRead(@NonNull File path, @NonNull String result, int currentPageNum, int pagesNum) {
        mCurrentFilePath = path;
        notifyObservers(observer -> observer.onFileReaded(result, currentPageNum, pagesNum), FileAsyncOperationsObserver.class);
    }

    @Override
    public void onFileWritten(@NonNull File path) {
        mCurrentFilePath = path;
        notifyObservers(FileAsyncOperationsObserver::onFileWritten, FileAsyncOperationsObserver.class);
    }
}
