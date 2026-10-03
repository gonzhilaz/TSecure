/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.file.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import android.text.TextUtils;

import com.kavsdkexample.secure_storage.model.file.FileModel;
import com.kavsdkexample.secure_storage.presenter.file.FileAsyncOperationsObserver;
import com.kavsdkexample.secure_storage.presenter.file.FilePresenter;
import com.kavsdkexample.secure_storage.presenter.base.impl.SecureStorageAsyncOperationsObserverImpl;
import com.kavsdkexample.secure_storage.presenter.base.impl.SecureStoragePresenterImpl;
import com.kavsdkexample.secure_storage.view.file.FileView;
import com.kavsdkexample.secure_storage.view.file.FileViewState;

import java.io.File;

import javax.inject.Inject;

@UiThread
public final class FilePresenterImpl extends    SecureStoragePresenterImpl<FileView, FileViewState, FileModel>
                                     implements FilePresenter {

    @Inject
    FilePresenterImpl(@NonNull FileModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull FileView view, @Nullable FileViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        registerModelObserver(new FileAsyncOperationsObserverImpl(view));
        if (viewCreated) {
            if (state == null) {
                File file = mModel.getCurrentFilePath();
                if (file != null) {
                    String path = file.getAbsolutePath();
                    if (!TextUtils.isEmpty(path)) {
                        view.setPath(path);
                    }
                }
                view.setFileContent("", 0, 0);
            } else {
                view.setPath(state.getFilePath());
                view.setPassword(state.getPassword());
                view.setFileContent(state.getFileContent(),
                                    state.getCurrentFilePage(),
                                    state.getFilePagesCount());
            }
        }
    }

    @Override
    public void readFile(int pageNum) {
        FileView view = requireView();
        view.setOperationsButtonsEnabled(false);
        mModel.readFile(view.getPath(), view.getPassword(), pageNum);
    }

    @Override
    public void writeFile(boolean append) {
        FileView view = requireView();
        view.setOperationsButtonsEnabled(false);
        String path = view.getPath();
        File file = new File(path);
        if (file.exists()) {
            view.showOverwriteDialog(append);
        } else {
            mModel.writeFile(path, view.getPassword(), view.getFileContent(), append);
        }
    }

    @Override
    public void acceptOverwrite(boolean append) {
        FileView view = requireView();
        mModel.writeFile(view.getPath(), view.getPassword(), view.getFileContent(), append);
    }

    @Override
    public void discardOverwrite() {
        FileView view = requireView();
        view.setOperationsButtonsEnabled(true);
    }

    @UiThread
    private static class FileAsyncOperationsObserverImpl extends SecureStorageAsyncOperationsObserverImpl<FileView>
                                                         implements FileAsyncOperationsObserver {
        FileAsyncOperationsObserverImpl(@NonNull FileView view) {
            super(view);
        }

        @Override
        public void onFileReaded(@NonNull String result, int currentPageNum, int pagesNum) {
            mView.setOperationsButtonsEnabled(true);
            mView.setFileContent(result, currentPageNum, pagesNum);
        }

        @Override
        public void onFileWritten() {
            mView.setOperationsButtonsEnabled(true);
            mView.showFileWritten();
        }
    }
}
