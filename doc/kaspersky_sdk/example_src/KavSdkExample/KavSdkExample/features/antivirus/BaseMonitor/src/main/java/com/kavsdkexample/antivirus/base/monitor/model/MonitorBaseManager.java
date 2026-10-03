/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.model;

import androidx.annotation.NonNull;

public interface MonitorBaseManager {
    void    init();
    void    applyMonitorState(boolean enabled, @NonNull MonitorStateObserver observer);
    boolean getMonitorState();

    void    applyCloudOnlyCheck(boolean enabled);
    void    applyCloudCheck(boolean enabled);
    void    applyCheckRiskware(boolean enabled);
    void    applySuspiciousCheck(boolean enabled);
    void    applyMaxFileCheckSize(long size);

    interface MonitorStateObserver {
        void onMonitorState(boolean enabled);
    }
}
