/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.presenter.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.monitor.presenter.impl.MonitorBasePresenterImpl;
import com.kavsdkexample.antivirus.rtp_monitor.model.RtpMonitorModel;
import com.kavsdkexample.antivirus.rtp_monitor.presenter.RtpMonitorPresenter;
import com.kavsdkexample.antivirus.rtp_monitor.view.RtpMonitorView;
import com.kavsdkexample.core.app.view.BaseViewState;

import javax.inject.Inject;

public class RtpMonitorPresenterImpl extends    MonitorBasePresenterImpl<RtpMonitorView, BaseViewState, RtpMonitorModel>
                                     implements RtpMonitorPresenter {
    @Inject
    RtpMonitorPresenterImpl(@NonNull RtpMonitorModel model) {
        super(model);
    }

    @Override
    public void subscribe(@NonNull RtpMonitorView view, @Nullable BaseViewState state, boolean viewCreated) {
        super.subscribe(view, state, viewCreated);
        if (viewCreated) {
            view.setAvAction(mModel.getAvAction());
            view.setDetectSuspicious(mModel.getScanSuspicious());
        }
    }

    @Override
    public void setAvAction(@NonNull AvAction action) {
        mModel.setAvAction(action);
    }

}
