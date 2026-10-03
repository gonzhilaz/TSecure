/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.model;

import com.kavsdkexample.antivirus.base.model.AntivirusModel;

public interface MonitorBaseModel extends AntivirusModel {
    void    setMonitorState(boolean enabled);
    boolean getMonitorState();
    void    setMaxFileCheckSize(long size);
    long    getMaxFileCheckSize();
}
