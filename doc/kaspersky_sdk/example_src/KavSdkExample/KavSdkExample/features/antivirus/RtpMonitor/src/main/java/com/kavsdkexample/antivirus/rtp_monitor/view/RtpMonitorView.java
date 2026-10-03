/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.rtp_monitor.view;

import androidx.annotation.NonNull;

import com.kavsdkexample.antivirus.base.model.AvAction;
import com.kavsdkexample.antivirus.base.monitor.view.MonitorBaseView;

public interface RtpMonitorView extends MonitorBaseView {
    void setDetectSuspicious(boolean enabled);
    void setAvAction(@NonNull AvAction action);
}
