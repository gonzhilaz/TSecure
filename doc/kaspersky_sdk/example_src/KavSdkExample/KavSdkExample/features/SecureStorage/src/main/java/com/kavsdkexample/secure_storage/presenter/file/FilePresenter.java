/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.secure_storage.presenter.file;

import com.kavsdkexample.secure_storage.presenter.base.SecureStoragePresenter;
import com.kavsdkexample.secure_storage.view.file.FileView;
import com.kavsdkexample.secure_storage.view.file.FileViewState;

public interface FilePresenter extends SecureStoragePresenter<FileView, FileViewState>,
                                       OverwriteOperation {
    void readFile(int pageNum);
    void writeFile(boolean append);
}
