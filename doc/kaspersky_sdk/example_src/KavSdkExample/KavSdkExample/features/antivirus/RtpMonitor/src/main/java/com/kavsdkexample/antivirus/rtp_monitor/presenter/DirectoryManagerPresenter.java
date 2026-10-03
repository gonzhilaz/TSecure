/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.rtp_monitor.view.DirectoryManagerView;
import com.kavsdkexample.core.app.presenter.BasePresenter;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface DirectoryManagerPresenter extends BasePresenter<DirectoryManagerView, BaseViewState> {
    void addFolderForMonitoring(@NonNull String folder, int flags);
    void addExcludeFolder(@NonNull String folder);
    void addDefaultDirectories();
    void removeDefaultDirectories();
}
