/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.antivirus.pua_monitor.model;

import com.kavsdkexample.antivirus.base.monitor.model.MonitorBaseModel;

public interface PuaMonitorModel extends MonitorBaseModel {
    void    setMissedAppCheckState(boolean value);
    boolean getMissedAppCheckState();
}