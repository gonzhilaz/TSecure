/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.model.settings;

import com.kavsdkexample.antivirus.base.monitor.model.settings.MonitorBaseSettings;

public interface Settings extends MonitorBaseSettings {
    void    setMissedAppCheckState(boolean value);
    boolean getMissedAppCheckState();
}
