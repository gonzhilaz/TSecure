/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.view;

import com.kavsdkexample.antivirus.base.view.AntivirusBaseView;

public interface MonitorBaseView extends AntivirusBaseView {
    void setMonitorEnabledState(boolean enabled);
    void setAllowCloudCheckState(boolean enabled);
    void setCloudOnlyCheckState(boolean enabled);
    void setRiskwareCheckState(boolean enabled);
    void setMaxFileCheckSize(long size);
    void onMonitorStateChanged(boolean enabled);
}
