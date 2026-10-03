/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.folder_monitor.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.monitor.presenter.MonitorBasePresenter;
import com.kavsdkexample.antivirus.folder_monitor.view.FolderMonitorView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface FolderMonitorPresenter extends MonitorBasePresenter<FolderMonitorView, BaseViewState> {
    void enableTryCure(boolean value);
    void folderItemClicked(@NonNull String path);
    void addButtonClicked();
    void clearButtonClicked();
    void addFolder(@NonNull String folder);
}
