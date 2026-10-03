/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.base.monitor.model.settings;

import com.kavsdkexample.antivirus.base.model.settings.AvFeatureSettings;

public interface MonitorBaseSettings extends AvFeatureSettings {
    void    setMonitorState(boolean enabled);
    boolean getMonitorState();

    void    setMaxFileCheckSize(long size);
    long    getMaxFileCheckSize();
}
