/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.view;

import com.kavsdkexample.antivirus.base.monitor.view.MonitorBaseView;

public interface PuaMonitorView extends MonitorBaseView {
    void setProcessMissedState(boolean enabled);
}
