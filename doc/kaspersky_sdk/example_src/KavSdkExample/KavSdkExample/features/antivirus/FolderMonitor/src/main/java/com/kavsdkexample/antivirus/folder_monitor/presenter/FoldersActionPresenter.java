/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface FoldersActionPresenter extends BasePresenter<BaseView, BaseViewState> {
    void removeFolder(@NonNull String folder);
    void cleanFolders();
}