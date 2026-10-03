/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.sdk;

import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseManager;

public interface PuaMonitorManager extends MonitorBaseManager {
    void applyProcessMissedApps(boolean enabled);
}