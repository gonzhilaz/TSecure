/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.presenter.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.folder_monitor.model.FolderMonitorModel;
import com.kavsdkexample.antivirus.folder_monitor.presenter.FoldersActionPresenter;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public class FoldersActionPresenterImpl extends    BasePresenterImpl<BaseView, BaseViewState, FolderMonitorModel>
                                       implements  FoldersActionPresenter {
    @Inject
    FoldersActionPresenterImpl(@NonNull FolderMonitorModel model) {
        super(model);
    }

    @Override
    public void removeFolder(@NonNull String folder) {
        mModel.removeFolderFromMonitoring(folder);
    }

    @Override
    public void cleanFolders() {
        mModel.removeAllFolders();
    }
}
