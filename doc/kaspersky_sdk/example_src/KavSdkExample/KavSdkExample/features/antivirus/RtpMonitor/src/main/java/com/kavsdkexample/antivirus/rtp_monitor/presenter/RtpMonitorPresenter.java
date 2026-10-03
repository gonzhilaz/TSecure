/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.presenter;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.monitor.presenter.MonitorBasePresenter;
import com.kavsdkexample.antivirus.rtp_monitor.view.RtpMonitorView;
import com.kavsdkexample.core.app.view.BaseViewState;

public interface RtpMonitorPresenter extends MonitorBasePresenter<RtpMonitorView, BaseViewState> {
    void setAvAction(@NonNull AvAction action);
}
