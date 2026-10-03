/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.view.file.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.secure_storage.view.file.FileViewState;

public final class FileViewStateImpl implements FileViewState {
    @NonNull private final String  mFilePath;
    @NonNull private final String  mPassword;
    @NonNull private final String  mFileContent;
             private final int     mCurrentPage;
             private final int     mPagesCount;

    public FileViewStateImpl(@NonNull String  filePath,
                             @NonNull String  password,
                             @NonNull String  fileContent,
                                      int     currentPage,
                                      int     pagesCount) {
        mFilePath       = filePath;
        mPassword       = password;
        mFileContent    = fileContent;
        mCurrentPage    = currentPage;
        mPagesCount     = pagesCount;
    }

    @Override
    @NonNull
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    @NonNull
    public String getPassword() {
        return mPassword;
    }

    @Override
    @NonNull
    public String getFileContent() {
        return mFileContent;
    }

    @Override
    public int getCurrentFilePage() {
        return mCurrentPage;
    }

    @Override
    public int getFilePagesCount() {
        return mPagesCount;
    }
}
