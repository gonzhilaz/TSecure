/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.presenter.impl;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.rtp_monitor.model.RtpMonitorModel;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RemoveDialogPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RemoveType;
import com.kavsdkexample.core.app.presenter.impl.BasePresenterImpl;
import com.kavsdkexample.core.app.view.BaseView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public class RemoveDialogPresenterImpl extends    BasePresenterImpl<BaseView, BaseViewState, RtpMonitorModel>
                                       implements RemoveDialogPresenter {
    @Inject
    RemoveDialogPresenterImpl(@NonNull RtpMonitorModel model) {
        super(model);
    }

    @Override
    public void removeFolder(@NonNull RemoveType type, @NonNull String folder) {
        switch (type) {
            case Exclusion:
                mModel.removeExcludedFolder(folder);
                break;
            case FolderToMonitor:
                mModel.removeFolderFromMonitoring(folder);
                break;
            default:
                throw new IllegalStateException("Unknown remove type: " + type);
        }
    }
}
